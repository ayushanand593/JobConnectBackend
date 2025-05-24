package com.dcode.jobconnect.dto;

import java.util.List;

import lombok.Data;

@Data
public class JobApplicationSubmissionDTO {
    private Long jobId;
    private Boolean useExistingResume; // If true, use candidate's stored resume
    private String voluntaryDisclosures;
    private List<DisclosureAnswerDTO> disclosureAnswers;
    // The actual files will be uploaded separately via MultipartFile
}
