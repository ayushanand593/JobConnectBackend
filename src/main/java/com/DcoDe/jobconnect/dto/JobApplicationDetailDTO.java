package com.DcoDe.jobconnect.dto;

import java.time.LocalDateTime;

import com.DcoDe.jobconnect.enums.ApplicationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationDetailDTO {
    private Long id;
    private String jobId;
    private String jobTitle;
    private String companyName;
    private String resumeFileId;
    private String resumeFileName;
    private String coverLetterFileId;
    private String coverLetterFileName;
    private ApplicationStatus status;
    private String voluntaryDisclosures;
    private LocalDateTime appliedDate;
    private LocalDateTime lastUpdated;
}
