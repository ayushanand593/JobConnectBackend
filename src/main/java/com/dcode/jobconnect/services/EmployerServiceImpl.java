package com.dcode.jobconnect.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.dcode.jobconnect.dto.DisclosureQuestionDTO;
import com.dcode.jobconnect.dto.EmployerProfileDTO;
import com.dcode.jobconnect.dto.EmployerProfileUpdateDTO;
import com.dcode.jobconnect.dto.JobDTO;
import com.dcode.jobconnect.dto.SkillDTO;
import com.dcode.jobconnect.entities.Company;
import com.dcode.jobconnect.entities.EmployerProfile;
import com.dcode.jobconnect.entities.Job;
import com.dcode.jobconnect.entities.User;
import com.dcode.jobconnect.enums.UserRole;
import com.dcode.jobconnect.exceptions.ResourceNotFoundException;
import com.dcode.jobconnect.repositories.EmployerProfileRepository;
import com.dcode.jobconnect.repositories.JobRepository;
import com.dcode.jobconnect.repositories.UserRepository;
import com.dcode.jobconnect.services.interfaces.EmployeeServiceI;
import com.dcode.jobconnect.utils.SecurityUtils;

// import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployerServiceImpl implements EmployeeServiceI {

 private final EmployerProfileRepository employerProfileRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    // private final CompanyRepository companyRepository;

    @Override
    public EmployerProfileDTO getEmployerById(Long employerId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || !currentUser.getRole().equals(UserRole.ADMIN)) {
            throw new AccessDeniedException("Not authorized to access employer profile");
        }

        EmployerProfile profile = employerProfileRepository.findByUserId(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found"));

        
        return mapToEmployerProfileDTO(profile);
    }


     @Override
    public EmployerProfileDTO getCurrentEmployerProfile() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || !currentUser.getRole().equals(UserRole.EMPLOYER)) {
            throw new AccessDeniedException("Not authorized to access employer profile");
        }

        EmployerProfile profile = employerProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found"));

        return mapToEmployerProfileDTO(profile);
    }

     @Override
    @Transactional
    public EmployerProfileDTO updateProfile(EmployerProfileUpdateDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || !currentUser.getRole().equals(UserRole.EMPLOYER)) {
            throw new AccessDeniedException("Not authorized to update employer profile");
        }

        EmployerProfile profile = employerProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found"));

        // Update fields
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setPhone(dto.getPhone());
        profile.setJobTitle(dto.getJobTitle());
        // profile.setProfilePictureUrl(dto.getProfilePictureUrl()); 

        // Save updated profile
        profile = employerProfileRepository.save(profile);

        return mapToEmployerProfileDTO(profile);
    }

    @Override
@Transactional
public void deleteEmployerById(Long employerId) {
    // 1) Get the logged-in admin
        User currentAdmin = SecurityUtils.getCurrentUser();
        if (currentAdmin == null || !currentAdmin.getRole().equals(UserRole.ADMIN) || !currentAdmin.getRole().equals(UserRole.EMPLOYER)) {
            throw new AccessDeniedException("Not authorized");
        }

        // 2) Load the target user
        User toDelete = userRepository.findById(employerId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + employerId));

        // 3) Make sure they’re actually an EMPLOYER (not another ADMIN or CANDIDATE)
        if (!toDelete.getRole().equals(UserRole.EMPLOYER)) {
            throw new IllegalStateException("You can only delete employers.");
        }

        // 4) COMPANY-ID check: only allow if they belong to the same company
        Long adminCompanyId  = currentAdmin.getCompany().getId();
        Long targetCompanyId = toDelete.getCompany().getId();
        if (!adminCompanyId.equals(targetCompanyId)) {
            throw new AccessDeniedException("Cannot delete employer outside your company");
        }

        // 5) Delete their profile (if you don’t have a JPA cascade)
        employerProfileRepository.findByUserId(employerId)
            .ifPresent(employerProfileRepository::delete);

        // 6) (Later) delete jobs they created…

        // 7) Finally, delete the User row
        userRepository.delete(toDelete);
}

@Override
    public List<JobDTO> getJobsByEmployerId(Long employerId) {
        List<Job> jobs = jobRepository.findAllByPostedById(employerId);
        return jobs.stream().map(this::mapToJobDTO).collect(Collectors.toList());
    }


    private EmployerProfileDTO mapToEmployerProfileDTO(EmployerProfile profile) {
        EmployerProfileDTO dto = new EmployerProfileDTO();
        dto.setId(profile.getId());
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        dto.setEmail(profile.getUser().getEmail());
        dto.setPhone(profile.getPhone());
        dto.setJobTitle(profile.getJobTitle());
        dto.setProfilePictureUrl(profile.getProfilePictureUrl()); 

        Company company = profile.getUser().getCompany();
        if (company != null) {
            dto.setCompanyName(company.getCompanyName());
            dto.setCompanyId(company.getId());
        }

        return dto;
    }

     private JobDTO mapToJobDTO(Job job) {
    JobDTO dto = new JobDTO();
    dto.setId(job.getId()); // Set the ID
    dto.setJobId(job.getJobId());
    dto.setTitle(job.getTitle());
    dto.setLocation(job.getLocation());
    dto.setJobType(job.getJobType().toString());
    dto.setExperienceLevel(job.getExperienceLevel());
    dto.setDescription(job.getDescription()); // Set description
    dto.setRequirements(job.getRequirements()); // Set requirements
    dto.setResponsibilities(job.getResponsibilities()); // Set responsibilities
    dto.setSalaryRange(job.getSalaryRange()); // Set salaryRange

    if (job.getSkills() != null) {
        dto.setSkills(job.getSkills().stream()
                .map(skill -> {
                    SkillDTO skillDTO = new SkillDTO();
                    skillDTO.setId(skill.getId());
                    skillDTO.setName(skill.getName());
                    return skillDTO;
                })
                .collect(Collectors.toList()));
    } else {
        dto.setSkills(null);
    }

    dto.setStatus(job.getStatus());
    dto.setCreatedAt(job.getCreatedAt());
    dto.setUpdatedAt(job.getUpdatedAt());

    if (job.getCompany() != null) {
        dto.setCompanyName(job.getCompany().getCompanyName());
        dto.setCompanyId(job.getCompany().getId());
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

    return dto;
}
}
