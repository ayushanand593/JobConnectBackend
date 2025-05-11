package com.DcoDe.jobconnect.services;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.DcoDe.jobconnect.dto.EmployerProfileDTO;
import com.DcoDe.jobconnect.entities.Company;
import com.DcoDe.jobconnect.entities.EmployerProfile;
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.enums.UserRole;
import com.DcoDe.jobconnect.exceptions.ResourceNotFoundException;
import com.DcoDe.jobconnect.repositories.CompanyRepository;
import com.DcoDe.jobconnect.repositories.EmployerProfileRepository;
import com.DcoDe.jobconnect.repositories.UserRepository;
import com.DcoDe.jobconnect.services.interfaces.EmployeeServiceI;
import com.DcoDe.jobconnect.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployerServiceImpl implements EmployeeServiceI {

 private final EmployerProfileRepository employerProfileRepository;
    // private final UserRepository userRepository;
    // private final CompanyRepository companyRepository;


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
}
