package com.DcoDe.jobconnect.services;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.DcoDe.jobconnect.dto.CompanyDetailDTO;
import com.DcoDe.jobconnect.dto.CompanyRegistrationDTO;
import com.DcoDe.jobconnect.dto.EmployeeRegistrationDTO;
import com.DcoDe.jobconnect.dto.EmployerProfileDTO;
import com.DcoDe.jobconnect.entities.Company;
import com.DcoDe.jobconnect.entities.EmployerProfile;
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.enums.UserRole;
import com.DcoDe.jobconnect.repositories.CompanyRepository;
import com.DcoDe.jobconnect.repositories.EmployerProfileRepository;
import com.DcoDe.jobconnect.repositories.UserRepository;
import com.DcoDe.jobconnect.services.interfaces.CompanyServiceI;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyServiceI {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployerProfileRepository employerProfileRepository;

    @Override
    @Transactional
    public CompanyDetailDTO registerCompany(CompanyRegistrationDTO dto) {
        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Check if company unique ID already exists
        if (companyRepository.findByCompanyUniqueId(dto.getCompanyUniqueId()).isPresent()) {
            throw new RuntimeException("Company ID already exists");
        }

        // Create company
        Company company = new Company();
        company.setCompanyName(dto.getCompanyName());
        company.setCompanyUniqueId(dto.getCompanyUniqueId());
        company.setIndustry(dto.getIndustry());
        company.setSize(dto.getSize());
        company.setWebsite(dto.getWebsite());
        company.setDescription(dto.getDescription());
        company.setAdmins(new HashSet<>());

        // Save company to get ID
        company = companyRepository.save(company);

        // Create user
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserRole.ADMIN);
        user.setCompany(company);

        // Save user
        user = userRepository.save(user);

        // Add user as admin
        company.getAdmins().add(user);
        companyRepository.save(company);

        // Map to DTO and return
        return mapToCompanyDetailDTO(company);
    }

    @Override
    public CompanyDetailDTO getCompanyByUniqueId(String companyUniqueId) {
        Company company = companyRepository.findByCompanyUniqueId(companyUniqueId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Company not found with ID: " + companyUniqueId
                ));
        return mapToCompanyDetailDTO(company);
    }

     @Override
    public Optional<Company> findByCompanyUniqueId(String companyUniqueId) {
        return companyRepository.findByCompanyUniqueId(companyUniqueId);
    }

    @Override
    @Transactional
    public EmployerProfileDTO addEmployerToCompany(EmployeeRegistrationDTO dto) {
        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Find company by unique ID
        Company company = companyRepository.findByCompanyUniqueId(dto.getCompanyUniqueId())
                .orElseThrow(()-> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Company not found with ID: " + dto.getCompanyUniqueId()
                ));

        // Create user
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserRole.EMPLOYER);
        user.setCompany(company);

        // Save user
        user = userRepository.save(user);

        // Create employer profile
        EmployerProfile profile = new EmployerProfile();
        profile.setUser(user);
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());

        // Save employer profile
        profile = employerProfileRepository.save(profile);

        // Map to DTO and return
        EmployerProfileDTO profileDTO = new EmployerProfileDTO();
        profileDTO.setId(profile.getId());
        profileDTO.setFirstName(profile.getFirstName());
        profileDTO.setLastName(profile.getLastName());
        profileDTO.setEmail(user.getEmail());
        profileDTO.setCompanyName(company.getCompanyName());
        profileDTO.setCompanyId(company.getId());

        return profileDTO;
    }

       @Override
public User findCompanyAdminByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
}

       @Override
public User findEmployerByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
}

@Override
public List<EmployerProfileDTO> getCompanyEmployees(String companyUniqueId) {
    List<User> employees = userRepository.findWithProfilesByCompanyCompanyUniqueId(companyUniqueId);
    
    return employees.stream()
        .map(this::convertToEmployerProfileDTO)
        .collect(Collectors.toList());
}

    
        private CompanyDetailDTO mapToCompanyDetailDTO(Company company) {
        CompanyDetailDTO dto = new CompanyDetailDTO();
        dto.setId(company.getId());
        dto.setCompanyName(company.getCompanyName());
        dto.setCompanyUniqueId(company.getCompanyUniqueId());
        dto.setIndustry(company.getIndustry());
        dto.setSize(company.getSize());
        dto.setWebsite(company.getWebsite());
        dto.setDescription(company.getDescription());
        // dto.setLogoUrl(company.getLogoUrl());
        dto.setLogoUrl(company.getLogoFileId());
        dto.setBannerUrl(company.getBannerFileId());
        dto.setCreatedAt(company.getCreatedAt());
        return dto;
    }
    private EmployerProfileDTO convertToEmployerProfileDTO(User user) {
    EmployerProfileDTO dto = new EmployerProfileDTO();
    
    // Set user data
    dto.setId(user.getId());
    dto.setEmail(user.getEmail());
    
    // Set employer profile data if available
    if (user.getEmployerProfile() != null) {
        EmployerProfile profile = user.getEmployerProfile();
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        dto.setPhone(profile.getPhone());
        dto.setJobTitle(profile.getJobTitle());
        dto.setProfilePictureUrl(profile.getProfilePictureUrl());
    }
    
    // Set company data if available
    if (user.getCompany() != null) {
        dto.setCompanyName(user.getCompany().getCompanyName());
        dto.setCompanyId(user.getCompany().getId());
    }
    
    return dto;
}
}
