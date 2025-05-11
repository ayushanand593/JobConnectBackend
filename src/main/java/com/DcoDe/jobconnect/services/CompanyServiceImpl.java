package com.DcoDe.jobconnect.services;

import java.util.HashSet;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.DcoDe.jobconnect.dto.CompanyDetailDTO;
import com.DcoDe.jobconnect.dto.CompanyRegistrationDTO;
import com.DcoDe.jobconnect.entities.Company;
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.enums.UserRole;
import com.DcoDe.jobconnect.repositories.CompanyRepository;
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
    // private final EmployerProfileRepository employerProfileRepository;

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
        private CompanyDetailDTO mapToCompanyDetailDTO(Company company) {
        CompanyDetailDTO dto = new CompanyDetailDTO();
        dto.setId(company.getId());
        dto.setCompanyName(company.getCompanyName());
        dto.setCompanyUniqueId(company.getCompanyUniqueId());
        dto.setIndustry(company.getIndustry());
        dto.setSize(company.getSize());
        dto.setWebsite(company.getWebsite());
        dto.setDescription(company.getDescription());
        dto.setLogoUrl(company.getLogoUrl());
        dto.setCreatedAt(company.getCreatedAt());
        return dto;
    }
}
