package com.DcoDe.jobconnect.services.interfaces;

import java.time.LocalDate;
import java.util.List;

import com.DcoDe.jobconnect.dto.CandidateDashboardStatsDTO;
import com.DcoDe.jobconnect.dto.EmployerDashboardStatsDTO;
import com.DcoDe.jobconnect.dto.JobApplicationDetailDTO;

public interface DashboardServiceI {
     CandidateDashboardStatsDTO getCandidateDashboardStats(LocalDate startDate, LocalDate endDate);

     List<JobApplicationDetailDTO> getCandidateApplications();

     JobApplicationDetailDTO getCandidateApplicationDetail(Long applicationId);

      EmployerDashboardStatsDTO getEmployerDashboardStats(LocalDate startDate, LocalDate endDate);

     
}
