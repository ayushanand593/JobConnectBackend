package com.DcoDe.jobconnect.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.DcoDe.jobconnect.dto.CandidateDashboardStatsDTO;
import com.DcoDe.jobconnect.dto.JobApplicationDetailDTO;
import com.DcoDe.jobconnect.dto.CandidateDashboardStatsDTO.ApplicationSummaryDTO;
import com.DcoDe.jobconnect.entities.Candidate;
import com.DcoDe.jobconnect.entities.FileDocument;
import com.DcoDe.jobconnect.entities.Job;
import com.DcoDe.jobconnect.entities.JobApplication;
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.exceptions.ResourceNotFoundException;
import com.DcoDe.jobconnect.repositories.JobApplicationRepository;
import com.DcoDe.jobconnect.services.interfaces.DashboardServiceI;
import com.DcoDe.jobconnect.services.interfaces.FileStorageServiceI;
import com.DcoDe.jobconnect.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardServiceI {

    private final JobApplicationRepository applicationRepository;
    private final FileStorageServiceI fileStorageService;

     @Override
    public CandidateDashboardStatsDTO getCandidateDashboardStats(LocalDate startDate, LocalDate endDate) {
    User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null || currentUser.getCandidateProfile() == null) {
        throw new AccessDeniedException("Not authorized to view candidate dashboard statistics");
    }

    Long candidateId = currentUser.getCandidateProfile().getId();
    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

    // Get all applications for the candidate
    List<JobApplication> allApplications = applicationRepository.findByCandidateId(candidateId);

    // Get applications within the date range
    List<JobApplication> periodApplications = allApplications.stream()
        .filter(app -> !app.getCreatedAt().isBefore(startDateTime) && !app.getCreatedAt().isAfter(endDateTime))
        .collect(Collectors.toList());

    return calculateCandidateStats(allApplications, periodApplications);
}

@Override
public List<JobApplicationDetailDTO> getCandidateApplications() {
    User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null || currentUser.getCandidateProfile() == null) {
        throw new AccessDeniedException("Not authorized to view applications");
    }

    List<JobApplication> applications = applicationRepository
        .findByCandidateId(currentUser.getCandidateProfile().getId());
    
    return applications.stream()
        .map(this::mapToJobApplicationDetailDTO)
        .collect(Collectors.toList());
}

@Override
public JobApplicationDetailDTO getCandidateApplicationDetail(Long applicationId) {
    User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null || currentUser.getCandidateProfile() == null) {
        throw new AccessDeniedException("Not authorized to view application details");
    }

    JobApplication application = applicationRepository.findById(applicationId)
        .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

    // Verify the application belongs to the current candidate
    if (!application.getCandidate().getId().equals(currentUser.getCandidateProfile().getId())) {
        throw new AccessDeniedException("Not authorized to view this application");
    }

    return mapToJobApplicationDetailDTO(application);
}

private CandidateDashboardStatsDTO calculateCandidateStats(List<JobApplication> allApplications, 
                                                          List<JobApplication> periodApplications) {
    CandidateDashboardStatsDTO stats = new CandidateDashboardStatsDTO();

    // Set total applications
    stats.setTotalApplications((long) allApplications.size());

    // Calculate applications by status
    Map<String, Long> statusDistribution = allApplications.stream()
        .collect(Collectors.groupingBy(
            app -> app.getStatus().name(),
            Collectors.counting()
        ));
    stats.setApplicationsByStatus(statusDistribution);

    // Get recent applications
    // List<ApplicationSummaryDTO> recentApplications = allApplications.stream()
    //     .sorted(Comparator.comparing(Application::getCreatedAt).reversed())
    //     .limit(5)
    //     .map(this::mapToApplicationSummary)
    //     .collect(Collectors.toList());
    // stats.setRecentApplications(recentApplications);

    // Calculate application trend
    Map<String, Long> applicationTrend = new LinkedHashMap<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    periodApplications.stream()
        .collect(Collectors.groupingBy(
            app -> app.getCreatedAt().format(formatter),
            Collectors.counting()
        ))
        .entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(entry -> applicationTrend.put(entry.getKey(), entry.getValue()));

    stats.setApplicationTrendByDate(applicationTrend);

    return stats;
}
private JobApplicationDetailDTO mapToJobApplicationDetailDTO(JobApplication application) {
    Job job = application.getJob();
    Candidate candidate = application.getCandidate();

    return JobApplicationDetailDTO.builder()
        .id(application.getId())
        .jobId(job.getJobId())
        .jobTitle(job.getTitle())
        .companyName(job.getCompany().getCompanyName())
        .resumeFileId(application.getResumeFileId())
        .resumeFileName(getFileNameFromFileId(application.getResumeFileId()))
        .coverLetterFileId(application.getCoverLetterFileId())
        .coverLetterFileName(getFileNameFromFileId(application.getCoverLetterFileId()))
        .status(application.getStatus())
        .voluntaryDisclosures(application.getVoluntaryDisclosures())
        .appliedDate(application.getCreatedAt())
        .lastUpdated(application.getUpdatedAt())
        .build();
}

private String getFileNameFromFileId(String fileId) {
    if (fileId == null) {
        return null;
    }
    try {
        FileDocument fileDocument = fileStorageService.getFile(fileId);
        return fileDocument.getFileName();
    } catch (Exception e) {
        // Log the error
        System.err.println("Error getting file name for fileId: " + fileId + " - " + e.getMessage());
        return "File Not Found"; // Or some other default value
    }
}
}
