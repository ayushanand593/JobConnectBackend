package com.DcoDe.jobconnect.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.DcoDe.jobconnect.entities.Job;
import com.DcoDe.jobconnect.enums.ApplicationStatus;
import com.DcoDe.jobconnect.repositories.DisclosureQuestionRepository;
import com.DcoDe.jobconnect.repositories.JobApplicationRepository;
import com.DcoDe.jobconnect.repositories.JobRepository;
import com.DcoDe.jobconnect.repositories.SavedJobRepository;
import com.DcoDe.jobconnect.services.interfaces.CleanupServiceI;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanupServiceImpl implements CleanupServiceI {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final SavedJobRepository savedJobRepository;  // You'll need this
    private final DisclosureQuestionRepository disclosureQuestionRepository;  // And this
    
    // This scheduled task runs daily at 2AM to remove withdrawn applications older than 15 days.
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanUpWithdrawnApplications() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(15);
            log.info("Starting cleanup of withdrawn applications older than {}", cutoff);
            
            // This requires a new repository method that filters at the database level
            int deletedCount = jobApplicationRepository.deleteByStatusAndUpdatedAtBefore(
                ApplicationStatus.WITHDRAWN, cutoff);
            
            log.info("Completed cleanup of withdrawn applications. Removed {} records", deletedCount);
        } catch (Exception e) {
            log.error("Error during withdrawn applications cleanup", e);
        }
    }
    
    // This scheduled task runs daily at 3AM to remove jobs whose applicationDeadline is older than 15 days.
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanUpOldJobs() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(15);
            log.info("Starting cleanup of jobs with application deadline before {}", cutoff);
            
            // Find jobs to delete at the database level
            List<Job> expiredJobs = jobRepository.findByApplicationDeadlineBefore(
                cutoff.toLocalDate().atTime(23, 59, 59));
            
            if (!expiredJobs.isEmpty()) {
                log.info("Found {} expired jobs to delete", expiredJobs.size());
                
                // Handle associated entities for each job
                for (Job job : expiredJobs) {
                    // Delete applications for this job
                    int appCount = jobApplicationRepository.NumberOfDeletedJob(job);
                    log.debug("Deleted {} applications for job {}", appCount, job.getId());
                    
                    // Delete saved jobs
                    int savedCount = savedJobRepository.NumberOfDeletedJob(job);
                    log.debug("Deleted {} saved job records for job {}", savedCount, job.getId());
                    
                    // Delete disclosure questions
                    int questionCount = disclosureQuestionRepository.NumberOfDeletedJob(job);
                    log.debug("Deleted {} disclosure questions for job {}", questionCount, job.getId());
                }
                
                // Now delete the jobs themselves
                jobRepository.deleteAll(expiredJobs);
                log.info("Successfully deleted {} expired jobs", expiredJobs.size());
            } else {
                log.info("No expired jobs found to delete");
            }
        } catch (Exception e) {
            log.error("Error during expired jobs cleanup", e);
        }
    }


}
