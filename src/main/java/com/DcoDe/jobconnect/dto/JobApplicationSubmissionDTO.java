package com.DcoDe.jobconnect.dto;

import lombok.Data;

@Data
public class JobApplicationSubmissionDTO {
    private Long jobId;
    private Boolean useExistingResume; // If true, use candidate's stored resume
    private String voluntaryDisclosures;
    // The actual files will be uploaded separately via MultipartFile
}
