package com.dcode.jobconnect.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.dcode.jobconnect.dto.JobDTO;
import com.dcode.jobconnect.dto.SavedJobDTO;
import com.dcode.jobconnect.entities.Job;
import com.dcode.jobconnect.entities.SavedJob;
import com.dcode.jobconnect.entities.User;
import com.dcode.jobconnect.services.FileStorageServiceImpl.LogoInfo;
import com.dcode.jobconnect.services.interfaces.FileStorageServiceI;
import com.dcode.jobconnect.services.interfaces.SavedJobServiceI;
import com.dcode.jobconnect.utils.SecurityUtils;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/saved-jobs")
@RequiredArgsConstructor
@Tag(name = "Saved Jobs", description = "API for managing saved jobs.")
public class SavedJobController {

    private final SavedJobServiceI savedJobService;
    private final FileStorageServiceI fileStorageService;

    /**
     * Save a job for the current candidate
     */
    @PostMapping("/{jobId}")
    @PreAuthorize("hasAuthority('CANDIDATE') or hasAuthority('ROLE_CANDIDATE')")
    @Operation(summary = "Save a job by it's jobId")
    public ResponseEntity<SavedJobDTO> saveJob(@PathVariable String jobId) {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        Long candidateId = user.getCandidateProfile().getId();

        try {
            SavedJob savedJob = savedJobService.saveJob(candidateId, jobId);
            SavedJobDTO savedJobDTO = convertToDTO(savedJob);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedJobDTO);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found", e);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving job: " + e.getMessage(), e);
        }
    }

    // Helper method to convert SavedJob entity to DTO
    private SavedJobDTO convertToDTO(SavedJob savedJob) {
       SavedJobDTO dto = new SavedJobDTO(
            savedJob.getId(),
            savedJob.getCandidate().getId(),
            savedJob.getJob().getJobId(),
            savedJob.getJob().getTitle(),
            savedJob.getJob().getCompany().getCompanyName(),
            savedJob.getSavedAt()
    );

    return dto;
    }

    /**
     * Remove a saved job
     */
    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasAuthority('CANDIDATE') or hasAuthority('ROLE_CANDIDATE')")
    @Operation(summary = "Remove a saved job by it's jobId")
    public ResponseEntity<String> unsaveJob(@PathVariable String jobId) {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        Long candidateId = user.getCandidateProfile().getId();

        try {
            savedJobService.unsaveJob(candidateId, jobId);
            return ResponseEntity.ok().body("Job successfully removed from saved jobs");
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error removing saved job: " + e.getMessage(), e);
        }
    }

    /**
     * Get all saved jobs for current candidate
     */
    @GetMapping
    @PreAuthorize("hasAuthority('CANDIDATE') or hasAuthority('ROLE_CANDIDATE')")
    @Operation(summary = "Get all saved jobs for the current candidate")
    public ResponseEntity<List<JobDTO>> getSavedJobs() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        Long candidateId = user.getCandidateProfile().getId();
        
        try {
            List<Job> savedJobs = savedJobService.getSavedJobsByCandidate(candidateId);
            List<JobDTO> jobDTOs = savedJobs.stream()
                .map(this::convertToJobDTO)
                .toList();
            return ResponseEntity.ok(jobDTOs);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching saved jobs", e);
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
          Long companyId = job.getCompany().getId();
    LogoInfo logoInfo = fileStorageService.getCompanyLogoInfo(companyId);
         if (logoInfo != null) {
        // 2. Set the logo fields on the DTO
        dto.setLogoFileId(logoInfo.getFileId());
        dto.setLogoBase64(logoInfo.getBase64Data());
        dto.setLogoContentType(logoInfo.getContentType());
        dto.setLogoFileName(logoInfo.getFileName());
        // dataUrl = "data:<MIME>;base64,<BASE64>"
        dto.setLogoDataUrl(logoInfo.getDataUrl());
    }
    return dto;
    }

    /**
     * Check if a job is saved
     */
    @GetMapping("/{jobId}/is-saved")
    @PreAuthorize("hasAuthority('CANDIDATE') or hasAuthority('ROLE_CANDIDATE')")
    @Operation(summary = "Check if a job is saved by the current candidate")
    public ResponseEntity<Boolean> isJobSaved(@PathVariable String jobId) {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        Long candidateId = user.getCandidateProfile().getId();
        
        try {
            boolean isSaved = savedJobService.isJobSavedByCandidate(candidateId, jobId);
            return ResponseEntity.ok(isSaved);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error checking saved status: " + e.getMessage(), e);
        }
    }
}