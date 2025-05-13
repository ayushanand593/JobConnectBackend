package com.DcoDe.jobconnect.controller;

import com.DcoDe.jobconnect.dto.JobApplicationDTO;
import com.DcoDe.jobconnect.dto.JobApplicationSubmissionDTO;
import com.DcoDe.jobconnect.dto.JobApplicationUpdateDTO;
import com.DcoDe.jobconnect.services.interfaces.JobApplicationServiceI;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationServiceI jobApplicationService;

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<JobApplicationDTO> submitApplication(
            @RequestPart("applicationData") JobApplicationSubmissionDTO submissionDTO,
            @RequestPart(value = "resumeFile", required = false) MultipartFile resumeFile,
            @RequestPart(value = "coverLetterFile", required = false) MultipartFile coverLetterFile) {
        
        JobApplicationDTO applicationDTO = jobApplicationService.submitApplication(
                submissionDTO, resumeFile, coverLetterFile);
        
        return ResponseEntity.ok(applicationDTO);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JobApplicationDTO> getApplication(@PathVariable Long id) {
        return ResponseEntity.ok(jobApplicationService.getJobApplication(id));
    }
    
    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Page<JobApplicationDTO>> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(jobApplicationService.getCurrentCandidateApplications(pageable));
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobApplicationDTO> updateApplicationStatus(
            @PathVariable Long id,
            @RequestBody JobApplicationUpdateDTO updateDTO) {
        
        return ResponseEntity.ok(jobApplicationService.updateApplicationStatus(id, updateDTO));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Void> withdrawApplication(@PathVariable Long id) {
        jobApplicationService.withdrawApplication(id);
        return ResponseEntity.noContent().build();
    }
}