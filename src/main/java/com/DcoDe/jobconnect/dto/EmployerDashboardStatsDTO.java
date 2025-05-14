package com.DcoDe.jobconnect.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class EmployerDashboardStatsDTO {
      private Long totalJobs;
    private Long openJobs;
    private Long closedJobs;
    private Long totalApplications;
    private Long newApplications; // Applications in period
    private Long shortlistedApplications;
    private Long rejectedApplications;

    // Application trend by date
    private Map<String, Long> applicationTrend;

    // Top performing jobs (most applications)
    private List<JobStatsDTO> topJobs;

    // Application status distribution
    private Map<String, Long> applicationStatusDistribution;

    // Job type distribution
    private Map<String, Long> jobTypeDistribution;

    @Data
    public static class JobStatsDTO {
        private Long jobId;
        private String title;
        private Long applicationCount;
    }
}
