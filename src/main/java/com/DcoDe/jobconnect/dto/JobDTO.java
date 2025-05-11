package com.DcoDe.jobconnect.dto;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobDTO {
    private Long id;
    private String jobId;
    private String title;
    private String companyName;
    private Long companyId;
    private String location;
    private String jobType;
    private String experienceLevel;
    private String description;
    private String requirements;
    private String responsibilities;
    private String salaryRange;
    private List<SkillDTO> skills;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
