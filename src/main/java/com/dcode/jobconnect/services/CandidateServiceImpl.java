package com.dcode.jobconnect.services;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.dcode.jobconnect.dto.CandidateProfileDTO;
import com.dcode.jobconnect.dto.CandidateProfileUpdateDTO;
import com.dcode.jobconnect.dto.CandidateRegistrationDTO;
import com.dcode.jobconnect.dto.SkillDTO;
import com.dcode.jobconnect.entities.Candidate;
import com.dcode.jobconnect.entities.FileDocument;
import com.dcode.jobconnect.entities.Skill;
import com.dcode.jobconnect.entities.User;
import com.dcode.jobconnect.enums.UserRole;
import com.dcode.jobconnect.exceptions.DuplicateEmailException;
import com.dcode.jobconnect.exceptions.ResourceNotFoundException;
import com.dcode.jobconnect.exceptions.TermsNotAcceptedException;
import com.dcode.jobconnect.repositories.CandidateRepository;
import com.dcode.jobconnect.repositories.DisclosureAnswerRepository;
import com.dcode.jobconnect.repositories.JobApplicationRepository;
import com.dcode.jobconnect.repositories.SavedJobRepository;
import com.dcode.jobconnect.repositories.SkillRepository;
import com.dcode.jobconnect.repositories.UserRepository;
import com.dcode.jobconnect.services.interfaces.CandidateServiceI;
import com.dcode.jobconnect.services.interfaces.FileStorageServiceI;
import com.dcode.jobconnect.utils.SecurityUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateServiceI {

        private final CandidateRepository candidateRepository;
           private final UserRepository userRepository;
        private final SkillRepository skillRepository;
            private final PasswordEncoder passwordEncoder;
             private final FileStorageServiceI fileStorageService; 
              private final JobApplicationRepository jobApplicationRepository;
    private final DisclosureAnswerRepository disclosureAnswerRepository;
    private final CleanupServiceImpl cleanupServiceImpl;

    private final SavedJobRepository savedJobRepository;
             private String errMsg = "Candidate profile not found ";


    @Override
    @Transactional
    public CandidateProfileDTO registerCandidate(CandidateRegistrationDTO dto) {
        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException("Email already registered");
        }
         if(!dto.isTermsAccepted()){
            throw new TermsNotAcceptedException("You must accept the terms and conditions to continue");
        }

        // Create user
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserRole.CANDIDATE);
        user.setTermsAccepted(true);
        user.setTermsAcceptedAt(LocalDateTime.now());

        // Save user to get ID
        user = userRepository.save(user);

        // Create candidate profile
        Candidate candidate = new Candidate();
        candidate.setUser(user);
        candidate.setFirstName(dto.getFirstName());
        candidate.setLastName(dto.getLastName());
        candidate.setPhone(dto.getPhone());
        candidate.setHeadline(dto.getHeadline());
        candidate.setSummary(dto.getSummary());
        candidate.setExperienceYears(dto.getExperienceYears());
        candidate.setSkills(new HashSet<>());

        // Save candidate profile
        candidate = candidateRepository.save(candidate);

        // Associate user with candidate profile
        user.setCandidateProfile(candidate);
        userRepository.save(user);

        // Add skills if provided
        if (dto.getSkills() != null && !dto.getSkills().isEmpty()) {
            Set<Skill> skills = getOrCreateSkills(dto.getSkills());
            candidate.setSkills(skills);
            candidateRepository.save(candidate);
        }

        // Map to DTO and return
        return mapToCandidateProfileDTO(candidate);
    }

     @Override
    public CandidateProfileDTO getCurrentCandidateProfile() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }

        Candidate candidate = candidateRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(errMsg));

        return mapToCandidateProfileDTO(candidate);
    }

    @Override
    public CandidateProfileDTO getCandidateById(Long id) {
        Candidate candidate = candidateRepository.findByIdWithSkills(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with ID: " + id));
        return mapToCandidateProfileDTO(candidate);
    }

    @Override
@Transactional
public CandidateProfileDTO updateCandidateProfile(CandidateProfileUpdateDTO profileDTO) {
    User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null) {
        throw new AccessDeniedException("Not authenticated");
    }

    Candidate candidate = candidateRepository.findByUserId(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException(errMsg));

    // Update candidate details
    candidate.setFirstName(profileDTO.getFirstName());
    candidate.setLastName(profileDTO.getLastName());
    candidate.setPhone(profileDTO.getPhone());
    candidate.setHeadline(profileDTO.getHeadline());
    candidate.setSummary(profileDTO.getSummary());
    candidate.setExperienceYears(profileDTO.getExperienceYears());

 

    // Update skills if provided
    if (profileDTO.getSkills() != null) {
        Set<Skill> skills = getOrCreateSkills(profileDTO.getSkills());
        candidate.setSkills(skills);
    }

    // Save updated candidate
    candidate = candidateRepository.save(candidate);

    return mapToCandidateProfileDTO(candidate);
}


   @Override
@Transactional
public void deleteCandidateById(Long candidateId) {
    User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null || !currentUser.getRole().equals(UserRole.CANDIDATE)) {
        throw new AccessDeniedException("Not authorized");
    }

    if (!currentUser.getId().equals(candidateId)) {
        throw new AccessDeniedException("You are not authorized to delete this candidate profile");
    }

    User toDelete = userRepository.findById(candidateId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + candidateId));

    if (!toDelete.getRole().equals(UserRole.CANDIDATE)) {
        throw new IllegalStateException("You can only delete candidates.");
    }

    Candidate candidate = candidateRepository.findByUserId(candidateId)
        .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

    // Delete related entities using bulk operations
    deleteCandidateRelatedEntitiesBulk(candidate.getId());

    // Delete the candidate profile
    candidateRepository.deleteById(candidate.getId());

    // Finally, delete the User record
    userRepository.deleteById(candidateId);
}

public CandidateProfileDTO uploadResume(MultipartFile file) {
    User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null) {
        throw new AccessDeniedException("Not authenticated");
    }

    Candidate candidate = candidateRepository.findByUserId(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException(errMsg));

    String oldResumeFileId = candidate.getResumeFileId();
    
    // Upload the new file
    String newResumeFileId = fileStorageService.uploadFile(file);
    
    // Update candidate with new resume file ID
    candidate.setResumeFileId(newResumeFileId);
    candidate = candidateRepository.save(candidate);
    
    // Schedule cleanup of old resume file (if it exists)
    if (oldResumeFileId != null) {
        scheduleFileCleanup(oldResumeFileId);
    }

    return mapToCandidateProfileDTO(candidate);
}
    @Override
public User findUserByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
}


    private CandidateProfileDTO mapToCandidateProfileDTO(Candidate candidate) {
        CandidateProfileDTO dto = new CandidateProfileDTO();
    dto.setId(candidate.getId());
    dto.setFirstName(candidate.getFirstName());
    dto.setLastName(candidate.getLastName());
    dto.setEmail(candidate.getUser().getEmail());
    dto.setPhone(candidate.getPhone());
    dto.setHeadline(candidate.getHeadline());
    dto.setSummary(candidate.getSummary());
    dto.setExperienceYears(candidate.getExperienceYears());
    dto.setCreatedAt(candidate.getUser().getCreatedAt());

    if (candidate.getResumeFileId() != null) {
        try {
            FileDocument fileDocument = fileStorageService.getFile(candidate.getResumeFileId());
            dto.setResumeFileId(candidate.getResumeFileId());
            dto.setResumeFileName(fileDocument.getFileName());
        } catch (Exception e) {
            // If file not found, just don't set the resume info
        }
    }

    List<SkillDTO> skillDTOs = mapToSkillDTOs(candidate.getSkills());
    dto.setSkills(skillDTOs);

    return dto;
}
    

    
    private List<SkillDTO> mapToSkillDTOs(Set<Skill> skills) {
        return skills.stream().map(skill -> {
            SkillDTO dto = new SkillDTO();
            dto.setId(skill.getId());
            dto.setName(skill.getName());
            return dto;
        }).toList();
    }

     private Set<Skill> getOrCreateSkills(List<String> skillNames) {
        Set<Skill> skills = new HashSet<>();

        for (String name : skillNames) {
            if (!StringUtils.hasText(name)) {
                continue;
            }

            Optional<Skill> existingSkill = skillRepository.findByNameIgnoreCase(name.trim());

            if (existingSkill.isPresent()) {
                skills.add(existingSkill.get());
            } else {
                Skill newSkill = new Skill();
                newSkill.setName(name.trim());
                newSkill = skillRepository.save(newSkill);
                skills.add(newSkill);
            }
        }

        return skills;
    }
@Transactional
public void deleteCandidateRelatedEntitiesBulk(Long candidateId) {
    // STEP 1: Get all job applications for this candidate
    List<Long> applicationIds = jobApplicationRepository.findApplicationIdsByCandidateId(candidateId);
    
    // STEP 2: Delete disclosure answers in batches
    if (!applicationIds.isEmpty()) {
        disclosureAnswerRepository.deleteByJobApplicationIds(applicationIds);
    }
    
    // STEP 3: Delete job applications by candidate ID
    jobApplicationRepository.deleteByCandidateId(candidateId);
    
    // STEP 4: Delete saved jobs by candidate ID
    savedJobRepository.deleteByCandidateId(candidateId);
    
    // STEP 5: Clear many-to-many relationships using native query
    candidateRepository.deleteCandidateSkillsByCandidateId(candidateId);
}
 private void scheduleFileCleanup(String fileId) {
        if (fileId != null) {
            cleanupServiceImpl.scheduleFileCleanup(fileId);
        }
    }
}
