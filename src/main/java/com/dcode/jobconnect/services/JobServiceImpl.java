package com.dcode.jobconnect.services;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dcode.jobconnect.dto.DisclosureAnswerDTO;
import com.dcode.jobconnect.dto.DisclosureQuestionDTO;
import com.dcode.jobconnect.dto.JobApplicationDTO;
import com.dcode.jobconnect.dto.JobApplicationSubmissionDTO;
import com.dcode.jobconnect.dto.JobCreateDTO;
import com.dcode.jobconnect.dto.JobDTO;
import com.dcode.jobconnect.dto.JobDisclosureQuestionsDTO;
import com.dcode.jobconnect.dto.SkillDTO;
import com.dcode.jobconnect.entities.Candidate;
import com.dcode.jobconnect.entities.DisclosureAnswer;
import com.dcode.jobconnect.entities.DisclosureQuestion;
import com.dcode.jobconnect.entities.FileDocument;
import com.dcode.jobconnect.entities.Job;
import com.dcode.jobconnect.entities.JobApplication;
import com.dcode.jobconnect.entities.Skill;
import com.dcode.jobconnect.entities.User;
import com.dcode.jobconnect.enums.ApplicationStatus;
import com.dcode.jobconnect.enums.JobStatus;
import com.dcode.jobconnect.enums.JobType;
import com.dcode.jobconnect.exceptions.FileNotFoundException;
import com.dcode.jobconnect.exceptions.FileProcessingException;
import com.dcode.jobconnect.exceptions.ResourceNotFoundException;
import com.dcode.jobconnect.repositories.CandidateRepository;
import com.dcode.jobconnect.repositories.DisclosureAnswerRepository;
import com.dcode.jobconnect.repositories.DisclosureQuestionRepository;
import com.dcode.jobconnect.repositories.JobApplicationRepository;
import com.dcode.jobconnect.repositories.JobRepository;
import com.dcode.jobconnect.repositories.SavedJobRepository;
import com.dcode.jobconnect.repositories.SkillRepository;
import com.dcode.jobconnect.services.interfaces.FileStorageServiceI;
import com.dcode.jobconnect.services.interfaces.JobServiceI;
import com.dcode.jobconnect.utils.SecurityUtils;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobServiceI {

    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final CandidateRepository candidateRepository;
    private final JobApplicationRepository jobApplicationRepository;

    private final DisclosureQuestionRepository disclosureQuestionRepository;
    
    private final DisclosureAnswerRepository disclosureAnswerRepository;

    private final FileStorageServiceI fileStorageService;

    private final SavedJobRepository savedJobRepository;


    private static final String JOB_NOT_FOUND = "Job not found with jobId: ";

     @Override
    @Transactional
    public JobDTO createJob(JobCreateDTO jobDto) {
       User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null || currentUser.getCompany() == null) {
        throw new AccessDeniedException("Not authorized to create jobs");
    }
    
    // Create new job entity
      Job job = new Job();
        job.setTitle(jobDto.getTitle());
        job.setLocation(jobDto.getLocation());
        job.setJobType(JobType.valueOf(jobDto.getJobType()));
        job.setExperienceLevel(jobDto.getExperienceLevel());
        job.setDescription(jobDto.getDescription());
        job.setRequirements(jobDto.getRequirements());
        job.setResponsibilities(jobDto.getResponsibilities());
        job.setSalaryRange(jobDto.getSalaryRange());
        job.setStatus(JobStatus.OPEN);
        job.setCompany(currentUser.getCompany());
        job.setPostedBy(currentUser);
         job.setApplicationDeadline(jobDto.getApplicationDeadline());

        // Generate a unique job ID
          job.setJobId(generateJobId(currentUser.getCompany().getCompanyName(), jobDto.getTitle()));

        // Handle skills
       if (jobDto.getSkills() != null && !jobDto.getSkills().isEmpty()) {
        // Instead of modifying the collection while iterating, create a new set
        Set<Skill> jobSkills = new HashSet<>();
        
        for (String skillName : jobDto.getSkills()) {
            // Find or create each skill
            Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                    .orElseGet(() -> {
                        Skill newSkill = new Skill();
                        newSkill.setName(skillName);
                        return skillRepository.save(newSkill);
                    });
            
            // Add to our new set
            jobSkills.add(skill);
        }
        
        // Now set the complete collection at once
        job.setSkills(jobSkills);
    } else {
        // Ensure we have an empty set rather than null
        job.setSkills(new HashSet<>());
    }

    // Handle disclosure questions
        addDisclosureQuestionsToJob(job, jobDto.getDisclosureQuestions());

        // Save job
        job = jobRepository.save(job);

        return mapToJobDTO(job);
    }

      public JobDTO getJobByJobId(String jobId) {
        Job job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with ID: " + jobId));
        return convertToJobDtoWithLogo(job);
    }

 public Page<JobDTO> getAllJobs(Pageable pageable) {
        Page<Job> jobs = jobRepository.findAll(pageable);
        return jobs.map(this::convertToJobDtoWithLogo);
    }

    @Override
    @Transactional
    public JobDTO updateJobByJobId(String jobId, JobCreateDTO jobDto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authorized to update jobs");
        }

        Job job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException(JOB_NOT_FOUND + jobId));

        // Check if user has permission to update this job
        if (!job.getCompany().getId().equals(currentUser.getCompany().getId()) || !job.getPostedBy().getId().equals(currentUser.getId()) ) {
            throw new AccessDeniedException("Not authorized to update this job");
        }

        return updateJobDetails(job, jobDto);
    }

@Override
    @Transactional
    public void deleteJobByJobId(String jobId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authorized to delete jobs");
        }

        Job job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        if (!job.getCompany().getId().equals(currentUser.getCompany().getId()) || 
            !job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Not authorized to delete this job");
        }
        
        // Use the proper deletion method
        deleteJobWithRelatedEntities(job);
    }

   @Override
@Transactional
public JobApplicationDTO applyToJob(String jobId, JobApplicationSubmissionDTO applicationCreateDTO, 
                                  MultipartFile resumeFile, MultipartFile coverLetterFile) {
    User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null) {
        throw new AccessDeniedException("Not authenticated");
    }

    Candidate candidate = findCandidateByUser(currentUser.getId());
    Job job = findJobById(jobId);
    
    validateJobApplication(job, candidate);
    
    JobApplication application = createJobApplication(job, candidate, applicationCreateDTO);
    handleResumeUpload(application, candidate, applicationCreateDTO, resumeFile);
    handleCoverLetterUpload(application, coverLetterFile);
    
    return saveApplicationWithDisclosures(application, job, applicationCreateDTO);
}
    @Override
    @Transactional
    public void changeJobStatusByJobId(String jobId, JobStatus status) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authorized to update job status");
        }

        Job job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException(JOB_NOT_FOUND + jobId));

        // Check if user has permission to update this job
        if (!job.getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new AccessDeniedException("Not authorized to update this job status");
        }

        job.setStatus(status);
        jobRepository.save(job);
    }


    @Override
       public JobDisclosureQuestionsDTO getJobDisclosureQuestions(String jobId) {
        Job job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException(JOB_NOT_FOUND + jobId));
                
        JobDisclosureQuestionsDTO dto = new JobDisclosureQuestionsDTO();
        dto.setJobId(job.getJobId());
        dto.setJobTitle(job.getTitle());
        dto.setCompanyName(job.getCompany().getCompanyName());
        
        List<DisclosureQuestionDTO> questionDTOs = job.getDisclosureQuestions().stream()
                .map(this::mapToDisclosureQuestionDTO)
                .collect(Collectors.toList());
        
        dto.setDisclosureQuestions(questionDTOs);
        
        return dto;
    }

    @Override
@Transactional
public JobDTO updateJobDisclosureQuestions(String jobId, List<DisclosureQuestionDTO> questions) {
    User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null || currentUser.getCompany() == null) {
        throw new AccessDeniedException("Not authorized to update job questions");
    }
    
    Job job = jobRepository.findByJobId(jobId)
            .orElseThrow(() -> new ResourceNotFoundException(JOB_NOT_FOUND + jobId));
    
    // Verify the job belongs to the current user's company
    if (!job.getCompany().getId().equals(currentUser.getCompany().getId())) {
        throw new AccessDeniedException("Not authorized to update this job's questions");
    }
    
    // Clear existing questions
    job.getDisclosureQuestions().clear();
    
    // Add new questions
    if (questions != null) {
        for (DisclosureQuestionDTO questionDTO : questions) {
            DisclosureQuestion question = new DisclosureQuestion();
            question.setJob(job);
            question.setQuestionText(questionDTO.getQuestionText());
            question.setIsRequired(Boolean.TRUE.equals(questionDTO.getIsRequired()));
            question.setCreatedAt(LocalDateTime.now());
            question.setUpdatedAt(LocalDateTime.now());
            
            job.getDisclosureQuestions().add(question);
        }
    }
    
    // Save job with updated questions
    job = jobRepository.save(job);
    
    return mapToJobDTO(job);
}

@Override
public Job getJobEntityByJobId(String jobId) {
    return jobRepository.findByJobId(jobId)
        .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));
}

     private void addDisclosureQuestionsToJob(Job job, List<DisclosureQuestionDTO> disclosureQuestionDTOs) {
        if (disclosureQuestionDTOs != null && !disclosureQuestionDTOs.isEmpty()) {
            // Clear existing questions
            job.getDisclosureQuestions().clear();
            
            // Add new questions
            for (DisclosureQuestionDTO questionDTO : disclosureQuestionDTOs) {
                DisclosureQuestion question = new DisclosureQuestion();
                question.setJob(job);
                question.setQuestionText(questionDTO.getQuestionText());
                question.setIsRequired(Boolean.TRUE.equals(questionDTO.getIsRequired()));
                question.setCreatedAt(LocalDateTime.now());
                question.setUpdatedAt(LocalDateTime.now());
                
                job.getDisclosureQuestions().add(question);
            }
        }
    }

    private JobDTO mapToJobDTO(Job job) {
        JobDTO dto = new JobDTO();
        dto.setId(job.getId());
        dto.setJobId(job.getJobId());
        dto.setTitle(job.getTitle());
        dto.setCompanyName(job.getCompany().getCompanyName());
        dto.setCompanyId(job.getCompany().getId());
        dto.setLocation(job.getLocation());
        dto.setJobType(job.getJobType().name());
        dto.setExperienceLevel(job.getExperienceLevel());
        dto.setDescription(job.getDescription());
        dto.setRequirements(job.getRequirements());
        dto.setResponsibilities(job.getResponsibilities());
        dto.setSalaryRange(job.getSalaryRange());
        dto.setApplicationDeadline(job.getApplicationDeadline());
        dto.setStatus(job.getStatus());
        dto.setCreatedAt(job.getCreatedAt());
        dto.setUpdatedAt(job.getUpdatedAt());

        // Map skills
        if (job.getSkills() != null) {
            List<SkillDTO> skillDTOs = job.getSkills().stream()
                    .map(skill -> {
                        SkillDTO skillDTO = new SkillDTO();
                        skillDTO.setId(skill.getId());
                        skillDTO.setName(skill.getName());
                        return skillDTO;
                    })
                    .collect(Collectors.toList());
            dto.setSkills(skillDTOs);
        }

         if (job.getDisclosureQuestions() != null) {
        List<DisclosureQuestionDTO> questionDTOs = job.getDisclosureQuestions().stream()
                .map(question -> {
                    DisclosureQuestionDTO questionDTO = new DisclosureQuestionDTO();
                    questionDTO.setId(question.getId());
                    questionDTO.setQuestionText(question.getQuestionText());
                    questionDTO.setIsRequired(question.getIsRequired());
                    return questionDTO;
                })
                .collect(Collectors.toList());
        dto.setDisclosureQuestions(questionDTOs);
    }
     try {
            FileStorageServiceImpl.LogoInfo logoInfo = fileStorageService.getCompanyLogoInfo(job.getCompany().getId());
            if (logoInfo != null) {
                dto.setLogoFileId(logoInfo.getFileId());
                dto.setLogoBase64(logoInfo.getBase64Data());
                dto.setLogoContentType(logoInfo.getContentType());
                dto.setLogoFileName(logoInfo.getFileName());
            }
        } catch (Exception e) {
            // Log the error but don't fail the entire request
            log.warn("Failed to load logo for company {}: {}", job.getCompany().getId(), e.getMessage());
        }

        return dto;
    }
      private JobDTO convertToJobDtoWithLogo(Job job) {
        // Convert basic job info
        JobDTO dto = mapToJobDTO(job);
        
        
        // Add logo information
        if (job.getCompany().getId() != null) {
            try {
                FileStorageServiceImpl.LogoInfo logoInfo = 
                    (FileStorageServiceImpl.LogoInfo) fileStorageService.getCompanyLogoInfo(job.getCompany().getId());
                
                if (logoInfo != null) {
                    dto.setLogoFileId(logoInfo.getFileId());
                    dto.setLogoBase64(logoInfo.getBase64Data());
                    dto.setLogoContentType(logoInfo.getContentType());
                    dto.setLogoFileName(logoInfo.getFileName());
                    dto.setLogoDataUrl(logoInfo.getDataUrl());
                    
                    log.debug("Logo loaded for company {}: {}", job.getCompany().getId(), logoInfo.getFileName());
                } else {
                    log.debug("No logo found for company {}", job.getCompany().getId());
                }
            } catch (Exception e) {
                log.warn("Failed to load logo for company {}: {}", job.getCompany().getId(), e.getMessage());
                // Don't fail the entire request, just continue without logo
            }
        }
        
        return dto;
    }
    
    // Method to update job with logo (for testing)
    public JobDTO refreshJobWithLogo(String jobId) {
        Job job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with ID: " + jobId));
        return convertToJobDtoWithLogo(job);
    }

   private JobDTO updateJobDetails(Job job, JobCreateDTO jobDto) {
        // Update job details
        job.setTitle(jobDto.getTitle());
        job.setLocation(jobDto.getLocation());
        job.setJobType(JobType.valueOf(jobDto.getJobType()));
        job.setExperienceLevel(jobDto.getExperienceLevel());
        job.setDescription(jobDto.getDescription());
        job.setRequirements(jobDto.getRequirements());
        job.setResponsibilities(jobDto.getResponsibilities());
        job.setSalaryRange(jobDto.getSalaryRange());
        job.setApplicationDeadline(jobDto.getApplicationDeadline());

        // Update skills
        if (jobDto.getSkills() != null) {
            job.getSkills().clear();
            for (String skillName : jobDto.getSkills()) {
                Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                        .orElseGet(() -> {
                            Skill newSkill = new Skill();
                            newSkill.setName(skillName);
                            return skillRepository.save(newSkill);
                        });
                job.getSkills().add(skill);
            }
        }
        addDisclosureQuestionsToJob(job, jobDto.getDisclosureQuestions());
        // Save updated job
        job = jobRepository.save(job);

        return mapToJobDTO(job);
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
                .toList();
        dto.setDisclosureAnswers(answerDTOs);
    }

        return dto;
    }

    private String generateJobId(String companyName, String jobTitle) {
        String baseId = companyName.replaceAll("\\s+", "-").toLowerCase() + "-" +
                jobTitle.replaceAll("\\s+", "-").toLowerCase();
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(6);
        return baseId + "-" + timestamp;
    }

    private DisclosureQuestionDTO mapToDisclosureQuestionDTO(DisclosureQuestion question) {
        DisclosureQuestionDTO dto = new DisclosureQuestionDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setIsRequired(question.getIsRequired());
        return dto;
    }
    // Validate that all required questions are answered
private void validateDisclosureAnswers(List<DisclosureQuestion> questions, List<DisclosureAnswerDTO> answers) {
    // Create a map of questionId -> answerDTO for easy lookup
    Map<Long, DisclosureAnswerDTO> answerMap = answers.stream()
            .collect(Collectors.toMap(DisclosureAnswerDTO::getQuestionId, Function.identity()));
    
    // Check that all required questions have answers
    for (DisclosureQuestion question : questions) {
        if (question.getIsRequired()) {
            DisclosureAnswerDTO answer = answerMap.get(question.getId());
            if (answer == null || StringUtils.isBlank(answer.getAnswerText())) {
                throw new IllegalArgumentException("Required question not answered: " + question.getQuestionText());
            }
        }
    }
}
private Candidate findCandidateByUser(Long userId) {
    return candidateRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
}

private Job findJobById(String jobId) {
    return jobRepository.findByJobId(jobId)
            .orElseThrow(() -> new ResourceNotFoundException(JOB_NOT_FOUND + jobId));
}

private void validateJobApplication(Job job, Candidate candidate) {
    validateJobStatus(job);
    validateExistingApplication(job, candidate);
    validateApplicationDeadline(job);
}

private void validateJobStatus(Job job) {
    if (job.getStatus() == JobStatus.CLOSED) {
        throw new IllegalArgumentException("This job is closed for applications");
    }
}

private void validateExistingApplication(Job job, Candidate candidate) {
    Optional<JobApplication> existingApplication = 
        jobApplicationRepository.findByJobIdAndCandidateId(job.getId(), candidate.getId());
    
    if (existingApplication.isPresent()) {
        ApplicationStatus status = existingApplication.get().getStatus();
        String errorMessage = status == ApplicationStatus.WITHDRAWN 
            ? "You have withdrawn your application for this job and cannot reapply"
            : "You have already applied to this job";
        throw new IllegalArgumentException(errorMessage);
    }
}

private void validateApplicationDeadline(Job job) {
    if (job.getApplicationDeadline() == null) {
        return;
    }
    
    LocalDateTime deadline = job.getApplicationDeadline().atTime(23, 59, 59);
    LocalDateTime now = LocalDateTime.now();
    
    if (now.isAfter(deadline)) {
        throw new IllegalArgumentException("The application deadline has ended");
    }
}

private JobApplication createJobApplication(Job job, Candidate candidate, 
                                          JobApplicationSubmissionDTO applicationCreateDTO) {
    JobApplication application = new JobApplication();
    application.setJob(job);
    application.setCandidate(candidate);
    application.setVoluntaryDisclosures(applicationCreateDTO.getVoluntaryDisclosures());
    application.setCreatedAt(LocalDateTime.now());
    application.setUpdatedAt(LocalDateTime.now());
    return application;
}

private void handleResumeUpload(JobApplication application, Candidate candidate, 
                                JobApplicationSubmissionDTO applicationCreateDTO, MultipartFile resumeFile) {
    boolean useExistingResume = Boolean.TRUE.equals(applicationCreateDTO.getUseExistingResume());
    
    if (useExistingResume) {
        if (candidate.getResumeFileId() == null) {
            throw new ResourceNotFoundException("Candidate does not have a stored resume");
        }
        
        // Create a COPY of the existing resume for this job application
        // This ensures the job application has its own file reference
        try {
            FileDocument originalResume = fileStorageService.getFile(candidate.getResumeFileId());
            String copiedResumeId = fileStorageService.copyFile(
                candidate.getResumeFileId(), 
                "JobApp_" + application.getId() + "_" + originalResume.getFileName()
            );
            application.setResumeFileId(copiedResumeId);
            
        } catch (Exception e) {
            throw new FileProcessingException("Failed to copy resume for job application", e);
        }
    } 
    else if (resumeFile != null && !resumeFile.isEmpty()) {
        // Upload new resume file directly for this application
        String resumeFileId = fileStorageService.uploadFile(resumeFile);
        application.setResumeFileId(resumeFileId);
    } else {
        throw new IllegalArgumentException("Resume is required");
    }
}

// private void validateAndUseExistingResume(JobApplication application, Candidate candidate) {
//     if (candidate.getResumeFileId() == null) {
//         throw new ResourceNotFoundException("Candidate does not have a stored resume");
//     }
//     application.setResumeFileId(candidate.getResumeFileId());
// }

private void handleCoverLetterUpload(JobApplication application, MultipartFile coverLetterFile) {
    if (coverLetterFile != null && !coverLetterFile.isEmpty()) {
        String coverLetterFileId = fileStorageService.uploadFile(coverLetterFile);
        application.setCoverLetterFileId(coverLetterFileId);
    }
}

private JobApplicationDTO saveApplicationWithDisclosures(JobApplication application, Job job, 
                                                        JobApplicationSubmissionDTO applicationCreateDTO) {
    if (applicationCreateDTO.getDisclosureAnswers() != null) {
        return saveApplicationWithAnswers(application, job, applicationCreateDTO);
    } else {
        validateRequiredDisclosures(job);
        application = jobApplicationRepository.save(application);
        return mapToJobApplicationDTO(application);
    }
}

private JobApplicationDTO saveApplicationWithAnswers(JobApplication application, Job job, 
                                                    JobApplicationSubmissionDTO applicationCreateDTO) {
    validateDisclosureAnswers(job.getDisclosureQuestions(), applicationCreateDTO.getDisclosureAnswers());
    application = jobApplicationRepository.save(application);
    saveDisclosureAnswers(application, job, applicationCreateDTO.getDisclosureAnswers());
    return mapToJobApplicationDTO(application);
}

private void validateRequiredDisclosures(Job job) {
    boolean hasRequiredQuestions = job.getDisclosureQuestions()
        .stream()
        .anyMatch(DisclosureQuestion::getIsRequired);
        
    if (hasRequiredQuestions) {
        throw new IllegalArgumentException("This job requires answers to disclosure questions");
    }
}

private void saveDisclosureAnswers(JobApplication application, Job job, 
                                  List<DisclosureAnswerDTO> disclosureAnswers) {
    for (DisclosureAnswerDTO answerDTO : disclosureAnswers) {
        DisclosureQuestion question = findAndValidateQuestion(answerDTO.getQuestionId(), job.getId());
        saveDisclosureAnswer(application, question, answerDTO);
    }
}

private DisclosureQuestion findAndValidateQuestion(Long questionId, Long jobId) {
    DisclosureQuestion question = disclosureQuestionRepository.findById(questionId)
            .orElseThrow(() -> new ResourceNotFoundException("Disclosure question not found"));
    
    if (!question.getJob().getId().equals(jobId)) {
        throw new IllegalArgumentException("Question does not belong to this job");
    }
    
    return question;
}

private void saveDisclosureAnswer(JobApplication application, DisclosureQuestion question, 
                                 DisclosureAnswerDTO answerDTO) {
    DisclosureAnswer answer = new DisclosureAnswer();
    answer.setJobApplication(application);
    answer.setQuestion(question);
    answer.setAnswerText(answerDTO.getAnswerText());
    answer.setCreatedAt(LocalDateTime.now());
    disclosureAnswerRepository.save(answer);
}
@Transactional
    public void deleteJobWithRelatedEntities(Job job) {
        // STEP 1: Delete disclosure answers first (they reference disclosure questions)
        disclosureAnswerRepository.deleteByJob(job);
        
        // STEP 2: Delete disclosure questions (they reference jobs)
        disclosureQuestionRepository.deleteByJobEntity(job);
        
        // STEP 3: Delete job applications (they reference jobs)
        jobApplicationRepository.deleteByJobEntity(job);
        
        // STEP 4: Delete saved jobs (they reference jobs)
        savedJobRepository.deleteSavedJobsByJob(job);
        
        // STEP 5: Clear many-to-many relationships (skills)
        job.getSkills().clear();
        jobRepository.save(job); // Save to persist cleared relationships
        
        // STEP 6: Finally delete the job
        jobRepository.delete(job);
    }
}
