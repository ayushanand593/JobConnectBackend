package com.DcoDe.jobconnect.services;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.DcoDe.jobconnect.dto.EmployerProfileDTO;
import com.DcoDe.jobconnect.dto.EmployerProfileUpdateDTO;
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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployerServiceImpl implements EmployeeServiceI {

 private final EmployerProfileRepository employerProfileRepository;
    private final UserRepository userRepository;
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
        if (currentAdmin == null || !currentAdmin.getRole().equals(UserRole.ADMIN)) {
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
