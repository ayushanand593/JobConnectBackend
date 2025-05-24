package com.dcode.jobconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

import com.dcode.jobconnect.enums.JobType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSearchResponseDTO {
    private Long id;
    private String jobId;
    private String title;
    private String companyName;
    private String companyLogoUrl;
    private String location;
    private JobType jobType;
    private String experienceLevel;
    private String salaryRange;
    private Set<String> skills;
    private LocalDateTime createdAt;
}
