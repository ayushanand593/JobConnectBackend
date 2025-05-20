package com.DcoDe.jobconnect.controller;

import com.DcoDe.jobconnect.dto.JobDTO;
import com.DcoDe.jobconnect.dto.SavedJobDTO;
import com.DcoDe.jobconnect.entities.Job;
import com.DcoDe.jobconnect.entities.SavedJob;
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.services.interfaces.SavedJobServiceI;
import com.DcoDe.jobconnect.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/saved-jobs")
@RequiredArgsConstructor
public class SavedJobController {

    private final SavedJobServiceI savedJobService;



    /**
     * Save a job for the current candidate
     */
 @PostMapping("/{jobId}")
@PreAuthorize("hasAuthority('CANDIDATE') || hasAuthority('ROLE_CANDIDATE')")
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
    @PreAuthorize("hasAuthority('CANDIDATE') || hasAuthority('ROLE_CANDIDATE')")
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
   @PreAuthorize("hasAuthority('CANDIDATE') || hasAuthority('ROLE_CANDIDATE')")
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
    @PreAuthorize("hasAuthority('CANDIDATE') || hasAuthority('ROLE_CANDIDATE')")
    public ResponseEntity<?> isJobSaved(@PathVariable String jobId,
                                       @RequestAttribute("candidateId") Long candidateId) {
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
