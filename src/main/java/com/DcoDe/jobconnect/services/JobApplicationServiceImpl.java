package com.DcoDe.jobconnect.services;

import com.DcoDe.jobconnect.dto.DisclosureAnswerDTO;
import com.DcoDe.jobconnect.dto.JobApplicationDTO;
import com.DcoDe.jobconnect.dto.JobApplicationSubmissionDTO;
import com.DcoDe.jobconnect.dto.JobApplicationUpdateDTO;
import com.DcoDe.jobconnect.entities.Candidate;
import com.DcoDe.jobconnect.entities.DisclosureAnswer;
import com.DcoDe.jobconnect.entities.FileDocument;
import com.DcoDe.jobconnect.entities.Job;
import com.DcoDe.jobconnect.entities.JobApplication;
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.enums.ApplicationStatus;
import com.DcoDe.jobconnect.exceptions.ResourceNotFoundException;
import com.DcoDe.jobconnect.repositories.CandidateRepository;
import com.DcoDe.jobconnect.repositories.DisclosureAnswerRepository;
import com.DcoDe.jobconnect.repositories.JobApplicationRepository;
import com.DcoDe.jobconnect.repositories.JobRepository;
import com.DcoDe.jobconnect.services.interfaces.FileStorageServiceI;
import com.DcoDe.jobconnect.services.interfaces.JobApplicationServiceI;
import com.DcoDe.jobconnect.utils.SecurityUtils;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationServiceI {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final FileStorageServiceI fileStorageService;
    private final DisclosureAnswerRepository disclosureAnswerRepository;
    private final JobApplicationRepository applicationRepository;

    /**
     * Submit a new job application
     */
    @Transactional
    public JobApplicationDTO submitApplication(
            JobApplicationSubmissionDTO submissionDTO,
            MultipartFile resumeFile,
            MultipartFile coverLetterFile) {
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }

        Candidate candidate = candidateRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        Job job = jobRepository.findById(submissionDTO.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // Check if already applied
        if (jobApplicationRepository.existsByJobIdAndCandidateId(job.getId(), candidate.getId())) {
            throw new RuntimeException("You have already applied to this job");
        }

        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setCandidate(candidate);
        application.setVoluntaryDisclosures(submissionDTO.getVoluntaryDisclosures());
        application.setCreatedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());

        // Handle resume
        if (submissionDTO.getUseExistingResume() != null && submissionDTO.getUseExistingResume()) {
            // Use candidate's stored resume
            if (candidate.getResumeFileId() == null) {
                throw new ResourceNotFoundException("Candidate does not have a stored resume");
            }
            application.setResumeFileId(candidate.getResumeFileId());
        } else if (resumeFile != null && !resumeFile.isEmpty()) {
            // Upload new resume
            String resumeFileId = fileStorageService.uploadFile(resumeFile);
            application.setResumeFileId(resumeFileId);
        } else {
            throw new RuntimeException("Resume is required");
        }

        // Handle cover letter
        if (coverLetterFile != null && !coverLetterFile.isEmpty()) {
            String coverLetterFileId = fileStorageService.uploadFile(coverLetterFile);
            application.setCoverLetterFileId(coverLetterFileId);
        }

        // Save application
        application = jobApplicationRepository.save(application);

        return mapToJobApplicationDTO(application);
    }

   
  
    @Transactional(readOnly = true)
    public List<JobApplicationDTO> getApplicationsForJob(String jobId) {
        // Fetch the job by jobId
        Job job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        // Get the current authenticated employer
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || !job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to view applications for this job");
        }

        // Fetch applications for the job
        List<JobApplication> applications = applicationRepository.findByJobId(jobId);

        // Map applications to ApplicationDTO
        return applications.stream()
                .map(this::mapToJobApplicationDTO)
                .collect(Collectors.toList());
    }
    /**
     * Get a specific job application
     */
    public JobApplicationDTO getJobApplication(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }

        JobApplication application = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found"));

        // Security check: only the candidate who applied or employer who posted the job can view
        Candidate candidate = candidateRepository.findByUserId(currentUser.getId()).orElse(null);

        if (candidate != null && application.getCandidate().getId().equals(candidate.getId())) {
            // It's the candidate who applied - allow access
            return mapToJobApplicationDTO(application);
        }

        // Check if the current user is the employer who posted the job
        Job job = application.getJob();
        if (currentUser.getRole().name().equals("EMPLOYER") && job.getPostedBy().getId().equals(currentUser.getId())) {
            // It's the employer who posted the job - allow access
            return mapToJobApplicationDTO(application);
        }

        // If none of the above conditions are met, deny access
        throw new AccessDeniedException("You don't have permission to view this application");
    }

    
    
    /**
     * Get all applications for the current candidate
     */
    public Page<JobApplicationDTO> getCurrentCandidateApplications(Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }

        Candidate candidate = candidateRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        Page<JobApplication> applications = jobApplicationRepository.findByCandidateId(candidate.getId(), pageable);
        return applications.map(this::mapToJobApplicationDTO);
    }

    /**
     * Update application status (usually done by employer)
     */
    @Transactional
    public JobApplicationDTO updateApplicationStatus(Long id, JobApplicationUpdateDTO updateDTO) {
        // Note: In a real application, you would add security checks to ensure
        // only the employer who posted the job can update the status
        
        JobApplication application = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found"));
        
        application.setStatus(updateDTO.getStatus());
        application.setUpdatedAt(LocalDateTime.now());
        
        application = jobApplicationRepository.save(application);
        
        return mapToJobApplicationDTO(application);
    }

    /**
     * Withdraw an application (by the candidate)
     */
    @Transactional
    public void withdrawApplication(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }

        Candidate candidate = candidateRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        JobApplication application = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found"));

        // Check if this application belongs to the current candidate
        if (!application.getCandidate().getId().equals(candidate.getId())) {
            throw new AccessDeniedException("You don't have permission to withdraw this application");
        }

        // Logic for withdrawal (could be delete or update status)
        jobApplicationRepository.delete(application);
    }

     @Override
@Transactional
public void updateApplicationStatus(Long id, ApplicationStatus status) {
    User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null) {
        throw new AccessDeniedException("Not authorized to update application status");
    }

    JobApplication application = applicationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

    // Check if the application belongs to user's company
    if (!application.getJob().getCompany().getId().equals(currentUser.getCompany().getId())) {
        throw new AccessDeniedException("Not authorized to update this application");
    }

    application.setStatus(status);
    applicationRepository.save(application);
}

@Override
public boolean isApplicationForEmployerJob(Long applicationId, Long employerId) {
    JobApplication application = jobApplicationRepository.findById(applicationId)
        .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));
    
    // Check if the job associated with this application belongs to the employer
    return application.getJob().getPostedBy().getId().equals(employerId);
}


    @Override
    public boolean isApplicationForJobPostedByEmployer(Long applicationId, Long employerId) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));
        Job job = application.getJob();
        System.out.println(job.getPostedBy().getId() + " " + employerId);
        return job.getPostedBy().getId().equals(employerId);
    }
    
    private JobApplicationDTO mapToJobApplicationDTO(JobApplication application) {
        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setId(application.getId());
        dto.setJobId(application.getJob().getId());
        dto.setCandidateId(application.getCandidate().getId());
        dto.setStatus(application.getStatus());
        dto.setVoluntaryDisclosures(application.getVoluntaryDisclosures());
        dto.setCreatedAt(application.getCreatedAt());
        dto.setUpdatedAt(application.getUpdatedAt());
        
        // Get resume info
        if (application.getResumeFileId() != null) {
            try {
                FileDocument resumeFile = fileStorageService.getFile(application.getResumeFileId());
                dto.setResumeFileId(application.getResumeFileId());
                dto.setResumeFileName(resumeFile.getFileName());
            } catch (Exception e) {
                // Log error but don't fail
            }
        }
        
        // Get cover letter info
        if (application.getCoverLetterFileId() != null) {
            try {
                FileDocument coverLetterFile = fileStorageService.getFile(application.getCoverLetterFileId());
                dto.setCoverLetterFileId(application.getCoverLetterFileId());
                dto.setCoverLetterFileName(coverLetterFile.getFileName());
            } catch (Exception e) {
                // Log error but don't fail
            }
        }

         List<DisclosureAnswer> answers = disclosureAnswerRepository.findAllByJobApplicationId(application.getId());
    if (answers != null && !answers.isEmpty()) {
        List<DisclosureAnswerDTO> answerDTOs = answers.stream()
                .map(answer -> {
                    DisclosureAnswerDTO answerDTO = new DisclosureAnswerDTO();
                    answerDTO.setQuestionId(answer.getQuestion().getId());
                    answerDTO.setQuestionText(answer.getQuestion().getQuestionText());
                    answerDTO.setAnswerText(answer.getAnswerText());
                    return answerDTO;
                })
                .collect(Collectors.toList());
        dto.setDisclosureAnswers(answerDTOs);
    }
        
        return dto;
    }
}