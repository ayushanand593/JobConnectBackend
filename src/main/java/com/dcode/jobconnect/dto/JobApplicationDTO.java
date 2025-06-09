package com.dcode.jobconnect.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import com.dcode.jobconnect.enums.ApplicationStatus;

@Data
public class JobApplicationDTO {
    private Long id;
    private Long jobId;
    private String jobName;
    private String companyName;
    private Long candidateId;
    private String candidateName;
    private Integer experienceYears;
    private String headline;
    private Set<String> skills;
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
