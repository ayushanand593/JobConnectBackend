package com.dcode.jobconnect.dto;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.dcode.jobconnect.enums.JobStatus;

@Data
public class JobDTO {
    private Long id;
    private String jobId;
    private String title;
    private String companyName;
    private Long companyId;

    private String logoFileId;
    private String logoBase64; // Base64 encoded logo data
    private String logoContentType; // MIME type of the logo
    private String logoFileName;

    private String logoDataUrl;
    private String location;
    private String jobType;
    private String experienceLevel;
    private String description;
    private String requirements;
    private String responsibilities;
    private String salaryRange;
    private List<SkillDTO> skills;
    private JobStatus status;
    private LocalDate applicationDeadline;
    private List<DisclosureQuestionDTO> disclosureQuestions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
