package com.dcode.jobconnect.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.dcode.jobconnect.dto.CompanyDetailDTO;
import com.dcode.jobconnect.dto.CompanyProfileUpdateDTO;
import com.dcode.jobconnect.dto.CompanyRegistrationDTO;
import com.dcode.jobconnect.dto.CompanyWithMediaDto;
import com.dcode.jobconnect.dto.EmployeeRegistrationDTO;
import com.dcode.jobconnect.dto.EmployerProfileDTO;
import com.dcode.jobconnect.entities.Company;
import com.dcode.jobconnect.entities.EmployerProfile;
import com.dcode.jobconnect.entities.FileDocument;
import com.dcode.jobconnect.entities.User;
import com.dcode.jobconnect.enums.UserRole;
import com.dcode.jobconnect.exceptions.CompanyNotFoundException;
import com.dcode.jobconnect.exceptions.DuplicateEmailException;
import com.dcode.jobconnect.exceptions.ResourceNotFoundException;
import com.dcode.jobconnect.exceptions.TermsNotAcceptedException;
import com.dcode.jobconnect.repositories.CompanyRepository;
import com.dcode.jobconnect.repositories.DisclosureAnswerRepository;
import com.dcode.jobconnect.repositories.DisclosureQuestionRepository;
import com.dcode.jobconnect.repositories.EmployerProfileRepository;
import com.dcode.jobconnect.repositories.JobApplicationRepository;
import com.dcode.jobconnect.repositories.JobRepository;
import com.dcode.jobconnect.repositories.SavedJobRepository;
import com.dcode.jobconnect.repositories.UserRepository;
import com.dcode.jobconnect.services.interfaces.CompanyServiceI;
import com.dcode.jobconnect.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyServiceImpl implements CompanyServiceI {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final SavedJobRepository savedJobRepository;
    private final DisclosureQuestionRepository disclosureQuestionRepository;
    private final DisclosureAnswerRepository disclosureAnswerRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployerProfileRepository employerProfileRepository;
    private final FileStorageServiceImpl fileStorageService;

    @PersistenceContext
    private final EntityManager em;

    @Override
    @Transactional
    public CompanyDetailDTO registerCompany(CompanyRegistrationDTO dto) {
        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException("Email already registered");
        }

        // Check if company unique ID already exists
        if (companyRepository.findByCompanyUniqueId(dto.getCompanyUniqueId()).isPresent()) {
            throw new CompanyNotFoundException("Company ID already exists");
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
        company.setLocation(dto.getLocation());
company.setAboutUs(dto.getAboutUs());
company.setBenefits(dto.getBenefits());

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
public List<CompanyWithMediaDto> searchCompaniesByName(String companyName) {
    List<Company> companies = companyRepository.findByCompanyNameContainingIgnoreCase(companyName);
    return companies.stream()
            .map(company -> {
                CompanyWithMediaDto dto = new CompanyWithMediaDto();
                // Map basic company fields
                dto.setId(company.getId());
                dto.setCompanyName(company.getCompanyName());
                dto.setCompanyUniqueId(company.getCompanyUniqueId());
                dto.setIndustry(company.getIndustry());
                dto.setSize(company.getSize());
                dto.setWebsite(company.getWebsite());
                dto.setDescription(company.getDescription());
                dto.setLocation(company.getLocation());
                dto.setAboutUs(company.getAboutUs());
                dto.setBenefits(company.getBenefits());
                dto.setCreatedAt(company.getCreatedAt());
                dto.setUpdatedAt(company.getUpdatedAt());

                // Add logo info
                try {
                    FileStorageServiceImpl.LogoInfo logoInfo = 
                        (FileStorageServiceImpl.LogoInfo) fileStorageService.getCompanyLogoInfo(company.getId());
                    if (logoInfo != null) {
                        dto.setLogoFileId(logoInfo.getFileId());
                        dto.setLogoBase64(logoInfo.getBase64Data());
                        dto.setLogoContentType(logoInfo.getContentType());
                        dto.setLogoFileName(logoInfo.getFileName());
                        dto.setLogoDataUrl(logoInfo.getDataUrl());
                    }
                } catch (Exception e) {
                    log.warn("Failed to load logo for company {}: {}", company.getId(), e.getMessage());
                }

                // Add banner info
                if (company.getBannerFileId() != null) {
                    try {
                        FileStorageServiceImpl.BannerInfo bannerInfo = 
                            ((FileStorageServiceImpl) fileStorageService).getBannerInfo(company.getBannerFileId());
                        if (bannerInfo != null) {
                            dto.setBannerFileId(bannerInfo.getFileId());
                            dto.setBannerBase64(bannerInfo.getBase64Data());
                            dto.setBannerContentType(bannerInfo.getContentType());
                            dto.setBannerFileName(bannerInfo.getFileName());
                            dto.setBannerDataUrl(bannerInfo.getDataUrl());
                        }
                    } catch (Exception e) {
                        log.warn("Failed to load banner for company {}: {}", company.getId(), e.getMessage());
                    }
                }
                return dto;
            })
            .collect(Collectors.toList());
}
  
     @Override
    public Optional<Company> findByCompanyUniqueId(String companyUniqueId) {
        return companyRepository.findByCompanyUniqueId(companyUniqueId);
    }

    @Override
    public Optional<Company> findById(Long companyId) {
        return companyRepository.findById(companyId);
    }
     @Override
    @Transactional
    public CompanyDetailDTO updateCompanyProfile(CompanyProfileUpdateDTO profileDTO) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getCompany() == null) {
            throw new AccessDeniedException("User not associated with any company");
        }

        Company company = currentUser.getCompany();

        // Check if user is admin
        if (!isCompanyAdmin(currentUser)) {
            throw new AccessDeniedException("Only company admins can update the profile");
        }

        // Update company details
        company.setCompanyName(profileDTO.getCompanyName());
        company.setDescription(profileDTO.getDescription());
        company.setWebsite(profileDTO.getWebsite());
        company.setIndustry(profileDTO.getIndustry());
        company.setSize(profileDTO.getSize());
        company.setLocation(profileDTO.getLocation());
company.setAboutUs(profileDTO.getAboutUs());
company.setBenefits(profileDTO.getBenefits());

        company = companyRepository.save(company);

        return mapToCompanyDetailDTO(company);
    }

    
@Override
@Transactional
public void deleteCompanyById(String companyUniqueId) {
    User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null || !isCompanyAdmin(currentUser)) {
        throw new AccessDeniedException("Only company admins can delete the company");
    }

    // Get company ID without loading the full entity
    Company company = companyRepository.findByCompanyUniqueId(companyUniqueId)
            .orElseThrow(() -> new ResourceNotFoundException("Company not found with unique ID: " + companyUniqueId));
    
    // Verify admin access using bulk query
    if (!companyRepository.isUserAdminOfCompany(currentUser.getId(), company.getId())) {
        throw new AccessDeniedException("Not authorized to delete this company");
    }
    
    // Delete everything using bulk operations - no entity loading
    deleteCompanyDataBulk(company.getId());
}

    @Override
    @Transactional
public User addEmployerToCompany(EmployeeRegistrationDTO dto) {
    // Check if user already exists
    if (userRepository.existsByEmail(dto.getEmail())) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, 
            "User with this email already exists"
        );
    }
    
    // Find the company
    Company company = findByCompanyUniqueId(dto.getCompanyUniqueId())
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Company not found with ID: " + dto.getCompanyUniqueId()
            ));
    
    // Create the User entity
    User user = new User();
    user.setEmail(dto.getEmail());
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    user.setRole(UserRole.EMPLOYER);
    user.setCompany(company);
    user.setTermsAccepted(dto.isTermsAccepted());
    user.setTermsAcceptedAt(LocalDateTime.now());
    
    // Save user first
    User savedUser = userRepository.save(user);
    
    // Create EmployerProfile
    EmployerProfile employerProfile = new EmployerProfile();
    employerProfile.setUser(savedUser);
    employerProfile.setFirstName(dto.getFirstName());
    employerProfile.setLastName(dto.getLastName() != null ? dto.getLastName() : ""); // Handle null lastName
    employerProfile.setPhone(""); // Default empty, can be updated later
    employerProfile.setJobTitle(""); // Default empty, can be updated later
    
    // Save employer profile
    employerProfileRepository.save(employerProfile);
    
    return savedUser;
}

       @Override
public User findCompanyAdminByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
}
 @Override
    public boolean isCompanyAdmin(User user) {
        if (user == null || user.getCompany() == null) {
            return false;
        }

        Company company = user.getCompany();

        // Check if company names match and user is in admins list
        return company.getAdmins().stream().anyMatch(admin -> admin.getId().equals(user.getId()));
    }


@Override
public User findEmployerByEmail(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    
    // Verify the user is an employer
    if (!user.getRole().equals(UserRole.EMPLOYER)) {
        throw new AccessDeniedException("User is not an employer");
    }
    
    return user;
}

@Override
public List<EmployerProfileDTO> getCompanyEmployees(String companyUniqueId) {
    List<User> employees = userRepository.findWithProfilesByCompanyCompanyUniqueId(companyUniqueId);
    
    return employees.stream()
        .map(this::convertToEmployerProfileDTO)
        .toList();
}
 
 @Transactional
    public Company updateCompanyBanner(Long companyId, MultipartFile bannerFile) throws IOException {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        
        // Delete old banner if exists
        if (company.getBannerFileId() != null) {
            ((FileStorageServiceImpl) fileStorageService).deleteBannerFile(company.getBannerFileId());
        }
        
        // Save new banner
        String newBannerFileId = ((FileStorageServiceImpl) fileStorageService).saveBannerFile(bannerFile);
        
        // Update company
        company.setBannerFileId(newBannerFileId);
        
        return companyRepository.save(company);
    }
    
    
    // @Transactional
    // public Company removeCompanyBanner(Long companyId) {
    //     Company company = companyRepository.findById(companyId)
    //             .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        
    //     // Delete banner file if exists
    //     if (company.getBannerFileId() != null) {
    //         ((FileStorageServiceImpl) fileStorageService).deleteBannerFile(company.getBannerFileId());
    //         company.setBannerFileId(null);
    //     }
        
    //     return companyRepository.save(company);
    // }
    
    // Get company with banner info

    @Transactional
    public Company removeCompanyBanner(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        
        // Delete banner file if exists
        if (company.getBannerFileId() != null) {
            ((FileStorageServiceImpl) fileStorageService).deleteBannerFile(company.getBannerFileId());
            company.setBannerFileId(null);
        }
        
        return companyRepository.save(company);
    }
    public CompanyWithMediaDto getCompanyWithMedia(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        
        CompanyWithMediaDto dto = new CompanyWithMediaDto();
        // Map basic company fields
        dto.setId(company.getId());
        dto.setCompanyName(company.getCompanyName());
        dto.setCompanyUniqueId(company.getCompanyUniqueId());
        dto.setIndustry(company.getIndustry());
        dto.setSize(company.getSize());
        dto.setWebsite(company.getWebsite());
        dto.setDescription(company.getDescription());
        dto.setLocation(company.getLocation());
        dto.setAboutUs(company.getAboutUs());
        dto.setBenefits(company.getBenefits());
        dto.setCreatedAt(company.getCreatedAt());
        dto.setUpdatedAt(company.getUpdatedAt());
        
        // Add logo info (existing logic)
        try {
            FileStorageServiceImpl.LogoInfo logoInfo = 
                (FileStorageServiceImpl.LogoInfo) fileStorageService.getCompanyLogoInfo(company.getId());
            if (logoInfo != null) {
                dto.setLogoFileId(logoInfo.getFileId());
                dto.setLogoBase64(logoInfo.getBase64Data());
                dto.setLogoContentType(logoInfo.getContentType());
                dto.setLogoFileName(logoInfo.getFileName());
                dto.setLogoDataUrl(logoInfo.getDataUrl());
            }
        } catch (Exception e) {
            log.warn("Failed to load logo for company {}: {}", companyId, e.getMessage());
        }
        
        // Add banner info
        if (company.getBannerFileId() != null) {
            try {
                FileStorageServiceImpl.BannerInfo bannerInfo = 
                    ((FileStorageServiceImpl) fileStorageService).getBannerInfo(company.getBannerFileId());
                if (bannerInfo != null) {
                    dto.setBannerFileId(bannerInfo.getFileId());
                    dto.setBannerBase64(bannerInfo.getBase64Data());
                    dto.setBannerContentType(bannerInfo.getContentType());
                    dto.setBannerFileName(bannerInfo.getFileName());
                    dto.setBannerDataUrl(bannerInfo.getDataUrl());
                    
                    log.debug("Banner loaded for company {}: {}", companyId, bannerInfo.getFileName());
                } else {
                    log.debug("Banner file not found for company {} with fileId: {}", companyId, company.getBannerFileId());
                }
            } catch (Exception e) {
                log.warn("Failed to load banner for company {}: {}", companyId, e.getMessage());
            }
        }
        
        return dto;
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
        dto.setLocation(company.getLocation());
dto.setAboutUs(company.getAboutUs());
dto.setBenefits(company.getBenefits());
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
@Transactional
private void deleteCompanyDataBulk(Long companyId) {
    // Clear user-company associations first
    userRepository.clearCompanyFromCandidates(companyId);
    userRepository.clearCompanyFromAdmins(companyId);
    
    // Delete in correct order - most dependent first
    // 1. Delete disclosure answers (references disclosure_questions)
    disclosureAnswerRepository.deleteByCompanyId(companyId);
    
    // 2. Delete job applications (references jobs)
    jobApplicationRepository.deleteByCompanyId(companyId);
    
    // 3. Delete saved jobs (references jobs)
    savedJobRepository.deleteByCompanyId(companyId);
    
    // 4. Clear job skills associations (many-to-many table)
    jobRepository.clearJobSkillsByCompanyId(companyId);
    
    // 5. Delete disclosure questions (references jobs) - BEFORE deleting jobs
    disclosureQuestionRepository.deleteByCompanyId(companyId);
    
    // 6. Now safe to delete jobs
    jobRepository.deleteByCompanyId(companyId);
    
    // 7. Delete employer profiles
    employerProfileRepository.deleteByCompanyId(companyId);
    
    // 8. Delete employer users
    userRepository.deleteEmployersByCompanyId(companyId);
    userRepository.deleteAllByCompanyId(companyId);
    
    // 9. Clear company admins (many-to-many table)
    companyRepository.clearCompanyAdmins(companyId);
    
    // 10. Finally delete the company
    companyRepository.deleteById(companyId);
    
    em.flush();
    em.clear();
}
}
