package com.DcoDe.jobconnect.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class CandidateDashboardStatsDTO {
    private Long totalApplications;
    private Map<String, Long> applicationsByStatus; // Count of applications by status
    private List<ApplicationSummaryDTO> recentApplications; // Last 5 applications
    private Map<String, Long> applicationTrendByDate; // Applications over time
    
    @Data
    public static class ApplicationSummaryDTO {
        private Long id;
        private String jobId;
        private String jobTitle;
        private String companyName;
        private String status;
        private LocalDateTime appliedDate;
        private LocalDateTime lastUpdated;
    }
}
