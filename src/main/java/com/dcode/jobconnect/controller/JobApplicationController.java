package com.dcode.jobconnect.controller;

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

import com.dcode.jobconnect.dto.JobApplicationDTO;
import com.dcode.jobconnect.dto.JobApplicationSubmissionDTO;
import com.dcode.jobconnect.dto.JobApplicationUpdateDTO;
import com.dcode.jobconnect.entities.User;
import com.dcode.jobconnect.services.interfaces.JobApplicationServiceI;
import com.dcode.jobconnect.utils.SecurityUtils;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Job Application", description = "API for managing job applications.")
public class JobApplicationController {

    private final JobApplicationServiceI jobApplicationService;

  
    // @PostMapping
    // @PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
    // @Operation(summary = "Submit a job application")
    // public ResponseEntity<JobApplicationDTO> submitApplication(
    //         @RequestPart("applicationData") JobApplicationSubmissionDTO submissionDTO,
    //         @RequestPart(value = "resumeFile", required = false) MultipartFile resumeFile,
    //         @RequestPart(value = "coverLetterFile", required = false) MultipartFile coverLetterFile) {
        
    //     User currentUser = SecurityUtils.getCurrentUser();
    //     if (currentUser == null) {
    //         throw new AccessDeniedException("Not authenticated");
    //     }
        
        
    //     JobApplicationDTO applicationDTO = jobApplicationService.submitApplication(
    //             submissionDTO, resumeFile, coverLetterFile);
        
    //     return ResponseEntity.ok(applicationDTO);
    // }
    
     @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a job application by ID")
    public ResponseEntity<JobApplicationDTO> getApplication(@PathVariable Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        // Get the application first to check ownership
        JobApplicationDTO application = jobApplicationService.getJobApplication(id);
    
        return ResponseEntity.ok(application);
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
    
     
}