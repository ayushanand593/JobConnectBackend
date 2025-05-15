package com.DcoDe.jobconnect.dto;

import com.DcoDe.jobconnect.enums.ApplicationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobApplicationDTO {
    private Long id;
    private Long jobId;
    private Long candidateId;
    private String resumeFileId;
    private String resumeFileName;
    private String coverLetterFileId;
    private String coverLetterFileName;
    private ApplicationStatus status;
    private String voluntaryDisclosures;
    private List<DisclosureAnswerDTO> disclosureAnswers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
