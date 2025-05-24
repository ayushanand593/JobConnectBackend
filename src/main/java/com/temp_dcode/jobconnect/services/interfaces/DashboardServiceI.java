package com.dcode.jobconnect.services.interfaces;

import java.time.LocalDate;
import java.util.List;

import com.dcode.jobconnect.dto.CandidateDashboardStatsDTO;
import com.dcode.jobconnect.dto.CompanyDashboardStatsDTO;
import com.dcode.jobconnect.dto.EmployerDashboardStatsDTO;
import com.dcode.jobconnect.dto.JobApplicationDetailDTO;

public interface DashboardServiceI {
     CandidateDashboardStatsDTO getCandidateDashboardStats(LocalDate startDate, LocalDate endDate);

     List<JobApplicationDetailDTO> getCandidateApplications();

     JobApplicationDetailDTO getCandidateApplicationDetail(Long applicationId);

      EmployerDashboardStatsDTO getEmployerDashboardStats(LocalDate startDate, LocalDate endDate);

      CompanyDashboardStatsDTO getCompanyDashboardStats(LocalDate startDate, LocalDate endDate);

     
}
