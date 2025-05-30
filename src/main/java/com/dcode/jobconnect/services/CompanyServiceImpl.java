package com.dcode.jobconnect.services;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.dcode.jobconnect.dto.CompanyDetailDTO;
import com.dcode.jobconnect.dto.CompanyProfileUpdateDTO;
import com.dcode.jobconnect.dto.CompanyRegistrationDTO;
import com.dcode.jobconnect.dto.EmployeeRegistrationDTO;
import com.dcode.jobconnect.dto.EmployerProfileDTO;
import com.dcode.jobconnect.entities.Company;
import com.dcode.jobconnect.entities.EmployerProfile;
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
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
    public EmployerProfileDTO addEmployerToCompany(EmployeeRegistrationDTO dto) {
        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException("Email already registered");
        }

        if(!dto.isTermsAccepted()){
            throw new TermsNotAcceptedException("You must accept the terms and conditions to continue");
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
        user.setTermsAccepted(true);
        user.setTermsAcceptedAt(LocalDateTime.now());

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
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
}

@Override
public List<EmployerProfileDTO> getCompanyEmployees(String companyUniqueId) {
    List<User> employees = userRepository.findWithProfilesByCompanyCompanyUniqueId(companyUniqueId);
    
    return employees.stream()
        .map(this::convertToEmployerProfileDTO)
        .toList();
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
