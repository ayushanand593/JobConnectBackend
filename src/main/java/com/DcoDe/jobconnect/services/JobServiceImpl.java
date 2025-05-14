package com.DcoDe.jobconnect.services;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.DcoDe.jobconnect.dto.JobApplicationDTO;
import com.DcoDe.jobconnect.dto.JobApplicationSubmissionDTO;
import com.DcoDe.jobconnect.dto.JobCreateDTO;
import com.DcoDe.jobconnect.dto.JobDTO;
import com.DcoDe.jobconnect.dto.SkillDTO;
import com.DcoDe.jobconnect.entities.Candidate;
import com.DcoDe.jobconnect.entities.FileDocument;
import com.DcoDe.jobconnect.entities.Job;
import com.DcoDe.jobconnect.entities.JobApplication;
import com.DcoDe.jobconnect.entities.Skill;
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.enums.JobStatus;
import com.DcoDe.jobconnect.enums.JobType;
import com.DcoDe.jobconnect.exceptions.ResourceNotFoundException;
import com.DcoDe.jobconnect.repositories.CandidateRepository;
import com.DcoDe.jobconnect.repositories.JobApplicationRepository;
import com.DcoDe.jobconnect.repositories.JobRepository;
import com.DcoDe.jobconnect.repositories.SkillRepository;
import com.DcoDe.jobconnect.services.interfaces.FileStorageServiceI;
import com.DcoDe.jobconnect.services.interfaces.JobServiceI;
import com.DcoDe.jobconnect.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobServiceI {

    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final CandidateRepository candidateRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final FileStorageServiceI fileStorageService;

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
    job.setExperienceLevel(jobDto.getExperienceLevel()); // Changed field name
    job.setDescription(jobDto.getDescription());
    // Don't set requirements and responsibilities if they're not in the DTO
    job.setRequirements(jobDto.getRequirements());
    job.setResponsibilities(jobDto.getResponsibilities());
    job.setSalaryRange(jobDto.getSalaryRange());
    job.setStatus(JobStatus.OPEN);
    job.setCompany(currentUser.getCompany());
    job.setPostedBy(currentUser);

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

        // Save job
        job = jobRepository.save(job);

        return mapToJobDTO(job);
    }

    @Override
    public JobDTO getJobByJobId(String jobId) {
        Job job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with jobId: " + jobId));
        return mapToJobDTO(job);
    }

    @Override
    @Transactional
    public JobDTO updateJobByJobId(String jobId, JobCreateDTO jobDto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authorized to update jobs");
        }

        Job job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with jobId: " + jobId));

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
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with jobId: " + jobId));

        // Check if user has permission to delete this job
        if (!job.getCompany().getId().equals(currentUser.getCompany().getId()) || !job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Not authorized to delete this job");
        }

        jobRepository.delete(job);
    }

    @Override
    @Transactional
    public JobApplicationDTO applyToJob(String jobId, JobApplicationSubmissionDTO applicationCreateDTO, MultipartFile resumeFile, MultipartFile coverLetterFile) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }

        Candidate candidate = candidateRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        Job job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with jobId: " + jobId));

        // Check if already applied
        if (jobApplicationRepository.existsByJobIdAndCandidateId(job.getId(), candidate.getId())) {
            throw new RuntimeException("You have already applied to this job");
        }

        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setCandidate(candidate);
        application.setVoluntaryDisclosures(applicationCreateDTO.getVoluntaryDisclosures());
        application.setCreatedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());

        // Handle resume
        if (applicationCreateDTO.getUseExistingResume() != null && applicationCreateDTO.getUseExistingResume()) {
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

    @Override
    @Transactional
    public void changeJobStatusByJobId(String jobId, JobStatus status) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authorized to update job status");
        }

        Job job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with jobId: " + jobId));

        // Check if user has permission to update this job
        if (!job.getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new AccessDeniedException("Not authorized to update this job status");
        }

        job.setStatus(status);
        jobRepository.save(job);
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

        return dto;
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

        return dto;
    }

    private String generateJobId(String companyName, String jobTitle) {
        String baseId = companyName.replaceAll("\\s+", "-").toLowerCase() + "-" +
                jobTitle.replaceAll("\\s+", "-").toLowerCase();
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(6);
        return baseId + "-" + timestamp;
    }

}
