package com.DcoDe.jobconnect.services;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.DcoDe.jobconnect.dto.CandidateProfileDTO;
import com.DcoDe.jobconnect.dto.CandidateRegistrationDTO;
import com.DcoDe.jobconnect.dto.SkillDTO;
import com.DcoDe.jobconnect.entities.Candidate;
import com.DcoDe.jobconnect.entities.Skill;
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.enums.UserRole;
import com.DcoDe.jobconnect.exceptions.ResourceNotFoundException;
import com.DcoDe.jobconnect.repositories.CandidateRepository;
import com.DcoDe.jobconnect.repositories.SkillRepository;
import com.DcoDe.jobconnect.repositories.UserRepository;
import com.DcoDe.jobconnect.services.interfaces.CandidateServiceI;
import com.DcoDe.jobconnect.utils.SecurityUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateServiceI {

        private final CandidateRepository candidateRepository;
           private final UserRepository userRepository;
        private final SkillRepository skillRepository;
            private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public CandidateProfileDTO registerCandidate(CandidateRegistrationDTO dto) {
        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Create user
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserRole.CANDIDATE);

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
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        return mapToCandidateProfileDTO(candidate);
    }

    @Override
    public CandidateProfileDTO getCandidateById(Long id) {
        Candidate candidate = candidateRepository.findByIdWithSkills(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with ID: " + id));
        return mapToCandidateProfileDTO(candidate);
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
        dto.setResumeUrl(candidate.getResumeUrl());
        dto.setCreatedAt(candidate.getUser().getCreatedAt());

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
        }).collect(Collectors.toList());
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

}
