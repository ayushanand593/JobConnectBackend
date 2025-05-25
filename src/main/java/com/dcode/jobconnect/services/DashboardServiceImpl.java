package com.dcode.jobconnect.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dcode.jobconnect.dto.CandidateDashboardStatsDTO;
import com.dcode.jobconnect.dto.CompanyDashboardStatsDTO;
import com.dcode.jobconnect.dto.EmployerDashboardStatsDTO;
import com.dcode.jobconnect.dto.JobApplicationDetailDTO;
import com.dcode.jobconnect.dto.CompanyDashboardStatsDTO.JobListItemDTO;
import com.dcode.jobconnect.entities.Company;
import com.dcode.jobconnect.entities.FileDocument;
import com.dcode.jobconnect.entities.Job;
import com.dcode.jobconnect.entities.JobApplication;
import com.dcode.jobconnect.entities.User;
import com.dcode.jobconnect.enums.ApplicationStatus;
import com.dcode.jobconnect.enums.JobStatus;
import com.dcode.jobconnect.exceptions.FileNotFoundException;
import com.dcode.jobconnect.exceptions.ResourceNotFoundException;
import com.dcode.jobconnect.repositories.JobApplicationRepository;
import com.dcode.jobconnect.repositories.JobRepository;
import com.dcode.jobconnect.repositories.UserRepository;
import com.dcode.jobconnect.services.interfaces.DashboardServiceI;
import com.dcode.jobconnect.services.interfaces.FileStorageServiceI;
import com.dcode.jobconnect.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardServiceI {

  
    private final JobApplicationRepository applicationRepository;

    private final JobRepository jobRepository;

    private final UserRepository userRepository;

    private final FileStorageServiceI fileStorageService;
        private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final int MAX_ITEMS_IN_LIST = 5;
    
    

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
        .toList();

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
        .toList();
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

@Override
    public EmployerDashboardStatsDTO getEmployerDashboardStats(LocalDate startDate, LocalDate endDate) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getCompany() == null) {
            throw new AccessDeniedException("Not authorized to view dashboard statistics");
        }

        Long employerId = currentUser.getId();
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        // Get all jobs posted by the employer
        List<Job> employerJobs = jobRepository.findAllByPostedById(employerId);

        if (employerJobs.isEmpty()) {
            return createEmptyStats();
        }

        Set<Long> jobIds = employerJobs.stream()
                .map(Job::getId)
                .collect(Collectors.toSet());

        // Get all applications for those jobs
        List<JobApplication> allApplications = applicationRepository.findAllByJobIdIn(jobIds);

        // Get applications within the date range
        List<JobApplication> periodApplications = allApplications.stream()
                .filter(app -> !app.getCreatedAt().isBefore(startDateTime) && !app.getCreatedAt().isAfter(endDateTime))
                .toList();

        return calculateEmployerStats(employerJobs, allApplications, periodApplications);
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

    // Calculate application trend
    Map<String, Long> applicationTrend = new LinkedHashMap<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

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
    @Override
    @Transactional(readOnly = true)
    public CompanyDashboardStatsDTO getCompanyDashboardStats(LocalDate startDate, LocalDate endDate) {
        // Get current user and verify they belong to a company
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getCompany() == null) {
            throw new AccessDeniedException("You must be associated with a company to access dashboard");
        }

        Company company = currentUser.getCompany();

        // Convert dates to LocalDateTime for queries
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        
        // Get all company jobs
        List<Job> allJobs = jobRepository.findByCompany(company);
        
        // Get job applications for the company in date range
        List<JobApplication> allApplications = applicationRepository.findByJobInAndCreatedAtBetween(
                allJobs, startDateTime, endDateTime);
        
        // Get company employers (users)
        List<User> employers = userRepository.findByCompany(company);
        
        // Get applications in the last month
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        long applicationsLastMonth = applicationRepository.countByJobInAndCreatedAtAfter(
                allJobs, oneMonthAgo);
        
        // Get jobs posted in the last month
        long jobsLastMonth = jobRepository.countByCompanyAndCreatedAtAfter(company, oneMonthAgo);
        
        // Calculate average applications per job
        double avgApplicationsPerJob = allJobs.isEmpty() ? 0 : 
                (double) allApplications.size() / allJobs.size();
        
        // Count open vs closed jobs
        long openJobs = allJobs.stream()
                .filter(job -> job.getStatus() == JobStatus.OPEN)
                .count();
        
        long closedJobs = allJobs.size() - openJobs;
        
        // Applications by date
        Map<String, Long> applicationsByDate = allApplications.stream()
                .collect(Collectors.groupingBy(
                        app -> app.getCreatedAt().toLocalDate().format(DATE_FORMATTER),
                        Collectors.counting()
                ));
        
        // Jobs by date
        Map<String, Long> jobsByDate = allJobs.stream()
                .filter(job -> job.getCreatedAt().isAfter(startDateTime) && 
                               job.getCreatedAt().isBefore(endDateTime))
                .collect(Collectors.groupingBy(
                        job -> job.getCreatedAt().toLocalDate().format(DATE_FORMATTER),
                        Collectors.counting()
                ));
        
        // Top jobs by application count
        Map<Job, Long> applicationCountsByJob = allApplications.stream()
                .collect(Collectors.groupingBy(JobApplication::getJob, Collectors.counting()));
        
        List<CompanyDashboardStatsDTO.JobStatsDTO> topJobsByApplications = applicationCountsByJob.entrySet().stream()
                .sorted(Map.Entry.<Job, Long>comparingByValue().reversed())
                .limit(MAX_ITEMS_IN_LIST)
                .map(entry -> CompanyDashboardStatsDTO.JobStatsDTO.builder()
                        .id(entry.getKey().getId())
                        .jobId(entry.getKey().getJobId())
                        .title(entry.getKey().getTitle())
                        .companyName(company.getCompanyName())
                        .jobType(entry.getKey().getJobType().toString())
                        .location(entry.getKey().getLocation())
                        .status(entry.getKey().getStatus().toString())
                        .applicationCount(entry.getValue())
                        .postedDate(entry.getKey().getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        // Applications by job type
        Map<String, Long> applicationsByJobType = allApplications.stream()
                .collect(Collectors.groupingBy(
                        app -> app.getJob().getJobType().toString(),
                        Collectors.counting()
                ));
        
        // Applications by location
        Map<String, Long> applicationsByLocation = allApplications.stream()
                .collect(Collectors.groupingBy(
                        app -> app.getJob().getLocation(),
                        Collectors.counting()
                ));
        
        // Applications by status
        Map<String, Long> applicationsByStatus = allApplications.stream()
                .collect(Collectors.groupingBy(
                        app -> app.getStatus().toString(),
                        Collectors.counting()
                ));
        
        // Recent employers
        List<CompanyDashboardStatsDTO.EmployerDTO> recentEmployers = employers.stream()
    .filter(user -> user.getEmployerProfile() != null) // 👈 Prevents NullPointerException
    .sorted(Comparator.comparing(User::getCreatedAt).reversed())
    .limit(MAX_ITEMS_IN_LIST)
    .map(user -> CompanyDashboardStatsDTO.EmployerDTO.builder()
        .id(user.getId())
        .fullName(user.getEmployerProfile().getFirstName() + " " + user.getEmployerProfile().getLastName())
        .email(user.getEmail())
        .joinDate(user.getCreatedAt())
        .position(user.getEmployerProfile().getJobTitle())
        .build())
    .collect(Collectors.toList());
        
        // Recent jobs
        List<JobListItemDTO> recentJobs = allJobs.stream()
                .sorted(Comparator.comparing(Job::getCreatedAt).reversed())
                .limit(MAX_ITEMS_IN_LIST)
                .map(job -> JobListItemDTO.builder()
                        .id(job.getId())
                        .jobId(job.getJobId())
                        .title(job.getTitle())
                        .jobType(job.getJobType().toString())
                        .location(job.getLocation())
                        .status(job.getStatus().toString())
                        .postedDate(job.getCreatedAt())
                        .applicationDeadline(job.getApplicationDeadline())
                        .build())
                .collect(Collectors.toList());
        
        // Build and return the dashboard DTO
        return CompanyDashboardStatsDTO.builder()
                .totalEmployers(employers.size())
                .totalJobs(allJobs.size())
                .totalApplications(allApplications.size())
                .applicationsLastMonth(applicationsLastMonth)
                .averageApplicationsPerJob(avgApplicationsPerJob)
                .openJobs(openJobs)
                .closedJobs(closedJobs)
                .jobsPostsLastMonth(jobsLastMonth)
                .applicationsByDate(applicationsByDate)
                .jobsByDate(jobsByDate)
                .topJobsByApplications(topJobsByApplications)
                .applicationsByJobType(applicationsByJobType)
                .applicationsByLocation(applicationsByLocation)
                .applicationsByStatus(applicationsByStatus)
                .recentEmployers(recentEmployers)
                .recentJobs(recentJobs)
                .build();
    }

 private EmployerDashboardStatsDTO calculateEmployerStats(List<Job> jobs, List<JobApplication> allApplications,
                                             List<JobApplication> periodApplications) {
        EmployerDashboardStatsDTO stats = new EmployerDashboardStatsDTO();

        // Calculate job stats
        stats.setTotalJobs((long) jobs.size());
        stats.setOpenJobs(jobs.stream()
                .filter(job -> job.getStatus() == JobStatus.OPEN)
                .count());
        stats.setClosedJobs(jobs.stream()
                .filter(job -> job.getStatus() == JobStatus.CLOSED || job.getStatus() == JobStatus.CLOSED)
                .count());

        // Calculate application stats
        stats.setTotalApplications((long) allApplications.size());
        stats.setNewApplications((long) periodApplications.size());
        stats.setShortlistedApplications(allApplications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.SHORTLISTED)
                .count());
        stats.setRejectedApplications(allApplications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.REJECTED)
                .count());

        // Application trend by date
        Map<String, Long> applicationTrend = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        periodApplications.stream()
                .collect(Collectors.groupingBy(
                        app -> app.getCreatedAt().format(formatter),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> applicationTrend.put(entry.getKey(), entry.getValue()));

        stats.setApplicationTrend(applicationTrend);

        // Top performing jobs
        Map<Long, Long> applicationCountByJob = allApplications.stream()
                .collect(Collectors.groupingBy(
                        app -> app.getJob().getId(),
                        Collectors.counting()
                ));

        List<EmployerDashboardStatsDTO.JobStatsDTO> topJobs = jobs.stream()
                .filter(job -> applicationCountByJob.containsKey(job.getId()))
                .map(job -> {
                    EmployerDashboardStatsDTO.JobStatsDTO jobStats = new EmployerDashboardStatsDTO.JobStatsDTO();
                    jobStats.setJobId(job.getId());
                    jobStats.setTitle(job.getTitle());
                    jobStats.setApplicationCount(applicationCountByJob.get(job.getId()));
                    return jobStats;
                })
                .sorted(Comparator.comparing(EmployerDashboardStatsDTO.JobStatsDTO::getApplicationCount).reversed())
                .limit(5)
                .collect(Collectors.toList());

        stats.setTopJobs(topJobs);

        // Application status distribution
        Map<String, Long> statusDistribution = allApplications.stream()
                .collect(Collectors.groupingBy(
                        app -> app.getStatus().name(),
                        Collectors.counting()
                ));

        stats.setApplicationStatusDistribution(statusDistribution);

        // Job type distribution
        Map<String, Long> jobTypeDistribution = jobs.stream()
                .collect(Collectors.groupingBy(
                        job -> job.getJobType().name(),
                        Collectors.counting()
                ));

        stats.setJobTypeDistribution(jobTypeDistribution);

        return stats;
    }

private JobApplicationDetailDTO mapToJobApplicationDetailDTO(JobApplication application) {
    Job job = application.getJob();

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
        throw new FileNotFoundException("File not found with ID: " + fileId, e);
    }
}

 private EmployerDashboardStatsDTO createEmptyStats() {
        EmployerDashboardStatsDTO stats = new EmployerDashboardStatsDTO();
        stats.setTotalJobs(0L);
        stats.setOpenJobs(0L);
        stats.setClosedJobs(0L);
        stats.setTotalApplications(0L);
        stats.setNewApplications(0L);
        stats.setShortlistedApplications(0L);
        stats.setRejectedApplications(0L);
        stats.setApplicationTrend(new HashMap<>());
        stats.setTopJobs(new ArrayList<>());
        stats.setApplicationStatusDistribution(new HashMap<>());
        stats.setJobTypeDistribution(new HashMap<>());
        return stats;
    }

}
