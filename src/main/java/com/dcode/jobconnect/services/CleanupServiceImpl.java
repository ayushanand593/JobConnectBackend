package com.dcode.jobconnect.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dcode.jobconnect.entities.FileDocument;
import com.dcode.jobconnect.entities.Job;
import com.dcode.jobconnect.enums.ApplicationStatus;
import com.dcode.jobconnect.repositories.DisclosureQuestionRepository;
import com.dcode.jobconnect.repositories.FileDocumentRepository;
import com.dcode.jobconnect.repositories.JobApplicationRepository;
import com.dcode.jobconnect.repositories.JobRepository;
import com.dcode.jobconnect.repositories.SavedJobRepository;
import com.dcode.jobconnect.services.interfaces.CleanupServiceI;
import com.dcode.jobconnect.services.interfaces.FileStorageServiceI;

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
   
    private FileStorageServiceI fileStorageService;
    

    private FileDocumentRepository fileDocumentRepository;
    
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
                    int appCount = jobApplicationRepository.Number_Of_Deleted_Job(job);
                    log.debug("Deleted {} applications for job {}", appCount, job.getId());
                    
                    // Delete saved jobs
                    int savedCount = savedJobRepository.Number_Of_Deleted_Job(job);
                    log.debug("Deleted {} saved job records for job {}", savedCount, job.getId());
                    
                    // Delete disclosure questions
                    int questionCount = disclosureQuestionRepository.Number_Of_Deleted_Job(job);
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

    @Async
    public void scheduleFileCleanup(String fileId) {
        try {
            // Wait a bit to ensure any ongoing operations complete
            Thread.sleep(5000); // 5 seconds delay
            
            cleanupUnusedFile(fileId);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("File cleanup interrupted for fileId: {}", fileId);
        }
    }
    
    /**
     * Cleans up a file if it's no longer in use
     */
    @Transactional
    public void cleanupUnusedFile(String fileId) {
        try {
            if (!fileStorageService.isFileInUse(fileId)) {
                fileStorageService.deleteFile(fileId);
                log.info("Successfully cleaned up unused file: {}", fileId);
            } else {
                log.debug("File {} is still in use, skipping cleanup", fileId);
            }
        } catch (Exception e) {
            log.error("Error during file cleanup for fileId: {}", fileId, e);
        }
    }
    
    /**
     * Scheduled cleanup job that runs periodically to clean up orphaned files
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void cleanupOrphanedFiles() {
        log.info("Starting scheduled cleanup of orphaned files");
        
        try {
            // Find snapshot files older than 1 day
            List<FileDocument> potentialOrphanFiles = fileDocumentRepository
                .findByIsSnapshotTrue();
            
            int cleanedCount = 0;
            for (FileDocument file : potentialOrphanFiles) {
                if (!fileStorageService.isFileInUse(file.getFileId())) {
                    fileStorageService.deleteFile(file.getFileId());
                    cleanedCount++;
                }
            }
            
            log.info("Scheduled cleanup completed. Cleaned {} orphaned files", cleanedCount);
            
        } catch (Exception e) {
            log.error("Error during scheduled file cleanup", e);
        }
    }


}
