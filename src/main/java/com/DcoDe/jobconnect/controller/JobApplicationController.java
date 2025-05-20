package com.DcoDe.jobconnect.controller;

import com.DcoDe.jobconnect.dto.JobApplicationDTO;
import com.DcoDe.jobconnect.dto.JobApplicationSubmissionDTO;
import com.DcoDe.jobconnect.dto.JobApplicationUpdateDTO;
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.services.interfaces.JobApplicationServiceI;
import com.DcoDe.jobconnect.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Job Application", description = "API for managing job applications.")
public class JobApplicationController {

    private final JobApplicationServiceI jobApplicationService;

  
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
    @Operation(summary = "Submit a job application")
    public ResponseEntity<JobApplicationDTO> submitApplication(
            @RequestPart("applicationData") JobApplicationSubmissionDTO submissionDTO,
            @RequestPart(value = "resumeFile", required = false) MultipartFile resumeFile,
            @RequestPart(value = "coverLetterFile", required = false) MultipartFile coverLetterFile) {
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        // Ensure candidate ID in submission matches current user
        // if (!submissionDTO.getCandidateId().equals(currentUser.getId())) {
        //     throw new AccessDeniedException("You can only submit applications for your own profile");
        // }
        
        JobApplicationDTO applicationDTO = jobApplicationService.submitApplication(
                submissionDTO, resumeFile, coverLetterFile);
        
        return ResponseEntity.ok(applicationDTO);
    }
    
     @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JobApplicationDTO> getApplication(@PathVariable Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        // Get the application first to check ownership
        JobApplicationDTO application = jobApplicationService.getJobApplication(id);
        
        // Check if current user is the candidate who submitted the application
        boolean isCandidate = currentUser.getRole().name().equals("CANDIDATE") && 
                             application.getCandidateId().equals(currentUser.getId());
        
        // Check if current user is the employer who owns the job
        // boolean isEmployer = jobApplicationService.isApplicationForJobPostedByEmployer(id, currentUser.getId());
        
        // // Allow access if user is admin, the candidate who applied, or the employer who owns the job
        // if (!isEmployer) {
        //     throw new AccessDeniedException("You don't have permission to view this application");
        // }
        
        return ResponseEntity.ok(application);
    }
    
    @GetMapping("/my-applications")
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
    public ResponseEntity<Page<JobApplicationDTO>> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(jobApplicationService.getCurrentCandidateApplications(pageable));
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    @Operation(summary = "Update the status of a job application")
    public ResponseEntity<JobApplicationDTO> updateApplicationStatus(
            @PathVariable Long id,
            @RequestBody JobApplicationUpdateDTO updateDTO) {
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        // Verify that the application belongs to a job owned by this employer
        if (!jobApplicationService.isApplicationForEmployerJob(id, currentUser.getId())) {
            throw new AccessDeniedException("You can only update applications for your own jobs");
        }
        
        return ResponseEntity.ok(jobApplicationService.updateApplicationStatus(id, updateDTO));
    }
    
     @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
    @Operation(summary = "Withdraw a job application")
    public ResponseEntity<Void> withdrawApplication(@PathVariable Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        // Verify that the application belongs to the current candidate
        JobApplicationDTO application = jobApplicationService.getJobApplication(id);
        if (!application.getCandidateId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only withdraw your own applications");
        }
        
        jobApplicationService.withdrawApplication(id);
        return ResponseEntity.noContent().build();
    }
}