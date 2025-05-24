package com.dcode.jobconnect.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.dcode.jobconnect.dto.JobDTO;
import com.dcode.jobconnect.dto.SavedJobDTO;
import com.dcode.jobconnect.entities.Job;
import com.dcode.jobconnect.entities.SavedJob;
import com.dcode.jobconnect.entities.User;
import com.dcode.jobconnect.services.interfaces.SavedJobServiceI;
import com.dcode.jobconnect.utils.SecurityUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/saved-jobs")
@RequiredArgsConstructor
@Tag (name = "Saved Jobs", description = "API for managing saved jobs.")
public class SavedJobController {

    private final SavedJobServiceI savedJobService;



    /**
     * Save a job for the current candidate
     */
 @PostMapping("/{jobId}")
@PreAuthorize("hasAuthority('CANDIDATE') or hasAuthority('ROLE_CANDIDATE')")
@Operation(summary = "Save a job by it's jobId")
public ResponseEntity<?> saveJob(@PathVariable String jobId) {
    User user = SecurityUtils.getCurrentUser();
    Long candidateId = user.getCandidateProfile().getId();

    try {
        SavedJob savedJob = savedJobService.saveJob(candidateId, jobId);
        // Convert to DTO before returning
        SavedJobDTO savedJobDTO = convertToDTO(savedJob);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedJobDTO);
    } catch (NoSuchElementException e) {
        return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error saving job: " + e.getMessage());
    }
}

// Helper method to convert SavedJob entity to DTO
private SavedJobDTO convertToDTO(SavedJob savedJob) {
    return new SavedJobDTO(
        savedJob.getId(),
        savedJob.getCandidate().getId(),
        savedJob.getJob().getJobId(),
        savedJob.getJob().getTitle(),
        savedJob.getJob().getCompany().getCompanyName(),
        savedJob.getSavedAt()
    );
}

    /**
     * Remove a saved job
     */
    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasAuthority('CANDIDATE') or hasAuthority('ROLE_CANDIDATE')")
    @Operation(summary = "Remove a saved job by it's jobId")
    public ResponseEntity<?> unsaveJob(@PathVariable String jobId) {
        User user = SecurityUtils.getCurrentUser();
    Long candidateId = user.getCandidateProfile().getId();

        try {
            savedJobService.unsaveJob(candidateId, jobId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error removing saved job: " + e.getMessage());
        }
    }

    /**
     * Get all saved jobs for current candidate
     */
    @GetMapping
   @PreAuthorize("hasAuthority('CANDIDATE') or hasAuthority('ROLE_CANDIDATE')")
   @Operation(summary = "Get all saved jobs for the current candidate")
   public ResponseEntity<?> getSavedJobs() {
            User user = SecurityUtils.getCurrentUser();
    Long candidateId = user.getCandidateProfile().getId();
        try {
            List<Job> savedJobs = savedJobService.getSavedJobsByCandidate(candidateId);
            // Convert Jobs to JobDTOs before returning
            List<JobDTO> jobDTOs = savedJobs.stream()
                .map(this::convertToJobDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(jobDTOs);
        } catch (Exception e) {
           throw new RuntimeException("Error fetching saved jobs: " + e.getMessage());
        }
    }
    private JobDTO convertToJobDTO(Job job) {
        JobDTO dto = new JobDTO();
        dto.setId(job.getId());
        dto.setJobId(job.getJobId());
        dto.setTitle(job.getTitle());
        dto.setCompanyName(job.getCompany().getCompanyName());
        dto.setLocation(job.getLocation());
        dto.setJobType(job.getJobType().name());
        dto.setExperienceLevel(job.getExperienceLevel());
        dto.setDescription(job.getDescription());
        // Set other fields as needed
        return dto;
    }

    /**
     * Check if a job is saved
     */
    @GetMapping("/{jobId}/is-saved")
    @PreAuthorize("hasAuthority('CANDIDATE') or hasAuthority('ROLE_CANDIDATE')")
    @Operation(summary = "Check if a job is saved by the current candidate")
    public ResponseEntity<?> isJobSaved(@PathVariable String jobId) {

          User user = SecurityUtils.getCurrentUser();
    Long candidateId = user.getCandidateProfile().getId();
        try {
            boolean isSaved = savedJobService.isJobSavedByCandidate(candidateId, jobId);
            return ResponseEntity.ok(isSaved);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking saved status: " + e.getMessage());
        }
    }
}
