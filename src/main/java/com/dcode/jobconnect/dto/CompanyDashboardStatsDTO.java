package com.dcode.jobconnect.dto;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class CompanyDashboardStatsDTO {
    // General stats
    private long totalEmployers;
    private long totalJobs;
    private long totalApplications;
    private long applicationsLastMonth;
    private double averageApplicationsPerJob;
    
    // Job stats
    private long openJobs;
    private long closedJobs;
    private long jobsPostsLastMonth;
    
    // Time-based stats
    private Map<String, Long> applicationsByDate; // Date string -> count
    private Map<String, Long> jobsByDate; // Date string -> count
    
    // Top stats
    private List<JobStatsDTO> topJobsByApplications; // Most applied to jobs
    private Map<String, Long> applicationsByJobType; // Job type -> count
    private Map<String, Long> applicationsByLocation; // Location -> count
    
    // Status stats
    private Map<String, Long> applicationsByStatus; // Status -> count
    
    // Quick access lists
    private List<EmployerDTO> recentEmployers; // Most recently added employers
    private List<JobListItemDTO> recentJobs; // Most recently posted jobs
    @Data
@Builder
public static class JobStatsDTO {
    private Long id;
    private String jobId;
    private String title;
    private String companyName;
    private String jobType;
    private String location;
    private String status;
    private long applicationCount;
    private LocalDateTime postedDate;
}

// DTO for employer listings
@Data
@Builder
public static class EmployerDTO {
    private Long id;
    private String fullName;
    private String email;
    private String position;
    private String department;
    private LocalDateTime joinDate;
}

// DTO for job listings
@Data
@Builder
public static class JobListItemDTO {
    private Long id;
    private String jobId;
    private String title;
    private String jobType;
    private String location;
    private String status;
    private LocalDateTime postedDate;
    private LocalDate applicationDeadline;
}
}