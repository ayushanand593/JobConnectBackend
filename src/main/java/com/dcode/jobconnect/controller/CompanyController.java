package com.dcode.jobconnect.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dcode.jobconnect.dto.CompanyDashboardStatsDTO;
import com.dcode.jobconnect.dto.CompanyDetailDTO;
import com.dcode.jobconnect.dto.CompanyProfileUpdateDTO;
import com.dcode.jobconnect.dto.CompanyRegistrationDTO;
import com.dcode.jobconnect.dto.CompanyWithMediaDto;
import com.dcode.jobconnect.dto.EmployerProfileDTO;
import com.dcode.jobconnect.dto.JwtResponseDTO;
import com.dcode.jobconnect.entities.Company;
import com.dcode.jobconnect.entities.FileDocument;
import com.dcode.jobconnect.entities.User;
import com.dcode.jobconnect.exceptions.ResourceNotFoundException;
import com.dcode.jobconnect.services.FileStorageServiceImpl;
import com.dcode.jobconnect.services.interfaces.AuthServiceI;
import com.dcode.jobconnect.services.interfaces.CompanyServiceI;
import com.dcode.jobconnect.services.interfaces.DashboardServiceI;
import com.dcode.jobconnect.services.interfaces.FileStorageServiceI;
import com.dcode.jobconnect.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name="Company", description = "API for managing company profiles and images.")
@Slf4j
public class CompanyController {

     private final CompanyServiceI companyService;
     private final FileStorageServiceI fileStorageService;

     private final DashboardServiceI dashboardService;

      private final AuthServiceI authService;

      private String errMsg = "Company not found with unique ID: ";

  @PostMapping("/register")
@Operation(summary = "Register a new company")
public ResponseEntity<JwtResponseDTO> registerCompany(@Valid @RequestBody CompanyRegistrationDTO dto) {
     companyService.registerCompany(dto);
    
    // Auto-login the company admin
    User user = companyService.findCompanyAdminByEmail(dto.getEmail());
    JwtResponseDTO authResponse = authService.generateTokenForUser(user);
    
    return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
}

    @GetMapping("/search")
@Operation(summary = "Search company profile by company name")
public ResponseEntity<List<CompanyWithMediaDto>> searchCompanyByName(
        @RequestParam String companyName) {
    try {
        List<CompanyWithMediaDto> companies = companyService.searchCompaniesByName(companyName);
        return ResponseEntity.ok(companies);
    } catch (Exception e) {
        log.error("Error searching companies with name {}: {}", companyName, e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
     @GetMapping("/{companyId}")
    @Operation(summary = "Get company profile with logo and banner")
    public ResponseEntity<CompanyWithMediaDto> getCompanyProfile(@PathVariable Long companyId) {
        try {
            CompanyWithMediaDto companyDto = companyService.getCompanyWithMedia(companyId);
            return ResponseEntity.ok(companyDto);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving company profile {}: {}", companyId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/profile")
   @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
   @Operation(summary = "Update company profile")
    public ResponseEntity<CompanyDetailDTO> updateProfile(
            @Valid @RequestBody CompanyProfileUpdateDTO profileDTO) {

        User currentUser = SecurityUtils.getCurrentUser();

        
        
        if (!companyService.isCompanyAdmin(currentUser)) {
            throw new AccessDeniedException("Only company admins can update the profile");
        }

        return ResponseEntity.ok(companyService.updateCompanyProfile(profileDTO));
    }

  @DeleteMapping("/{companyUniqueId}/delete")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
@Operation(summary = "Delete company by unique ID")
public ResponseEntity<String> deleteCompany(@PathVariable String companyUniqueId) {
    User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null) {
        throw new AccessDeniedException("Not authorized");
    }

    Company company = companyService.findByCompanyUniqueId(companyUniqueId)
            .orElseThrow(() -> new ResourceNotFoundException(errMsg + companyUniqueId));

    // Check if the current user is an admin of the company
    if (!company.getAdmins().contains(currentUser)) {
        throw new AccessDeniedException("Not authorized to delete this company");
    }

    // Just pass the company ID instead of the entire entity
    companyService.deleteCompanyById(companyUniqueId);
    return ResponseEntity.ok("Company deleted successfully");
}

     @PostMapping("/{companyId}/logo")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload company logo")
    public ResponseEntity<Map<String, String>> uploadCompanyLogo(
            @PathVariable Long companyId,
            @RequestParam("file") MultipartFile file) {

                 User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null) {
        throw new AccessDeniedException("Not authorized");
    }

// Optional<Company> company = companyService.findById(companyId);
    // Retrieve the company and check if current user is an admin of it
   Company company = companyService.findById(companyId)
            .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

    if (!company.getAdmins().contains(currentUser)) {
        throw new AccessDeniedException("Not authorized to update logo for this company");
    }
        
        try {
            // Validate file type
            if (!isImageFile(file)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only image files are allowed"));
            }
            
            // Save the logo
            String fileId = ((FileStorageServiceImpl) fileStorageService).saveCompanyLogo(companyId, file);
            
            return ResponseEntity.ok(Map.of(
                "message", "Logo uploaded successfully",
                "fileId", fileId,
                "companyId", companyId.toString()
            ));
            
        } catch (Exception e) {
            log.error("Error uploading logo for company {}: {}", companyId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to upload logo: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{companyId}/logo")
    @Operation(summary = "Get company logo")
    public ResponseEntity<?> getCompanyLogo(@PathVariable Long companyId) {
        try {
            Optional<FileDocument> logoDoc = fileStorageService.getCompanyLogo(companyId);
            
            if (logoDoc.isPresent()) {
                FileDocument doc = logoDoc.get();
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(doc.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                    .body(doc.getData());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error retrieving logo for company {}: {}", companyId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve logo"));
        }
    }
    
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

      @PostMapping("/{companyId}/banner")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload company banner")
    public ResponseEntity<Map<String, String>> uploadCompanyBanner(
            @PathVariable Long companyId,
            @RequestParam("file") MultipartFile file) {

        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authorized");
        }

        Company company = companyService.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        if (!company.getAdmins().contains(currentUser)) {
            throw new AccessDeniedException("Not authorized to update banner for this company");
        }
        
        try {
            if (!isImageFile(file)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only image files are allowed"));
            }
            
            Company updatedCompany = companyService.updateCompanyBanner(companyId, file);
            
            return ResponseEntity.ok(Map.of(
                "message", "Banner uploaded successfully",
                "fileId", updatedCompany.getBannerFileId(),
                "companyId", companyId.toString()
            ));
            
        } catch (Exception e) {
            log.error("Error uploading banner for company {}: {}", companyId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to upload banner: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{companyId}/banner")
    @Operation(summary = "Get company banner")
    public ResponseEntity<?> getCompanyBanner(@PathVariable Long companyId) {
        try {
            Company company = companyService.findById(companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
            
            if (company.getBannerFileId() == null) {
                return ResponseEntity.notFound().build();
            }
            
            Optional<FileDocument> bannerDoc = ((FileStorageServiceImpl) fileStorageService).getBannerByFileId(company.getBannerFileId());
            
            if (bannerDoc.isPresent()) {
                FileDocument doc = bannerDoc.get();
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(doc.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                    .body(doc.getData());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error retrieving banner for company {}: {}", companyId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve banner"));
        }
    }
    
    @DeleteMapping("/{companyId}/banner")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove company banner")
    public ResponseEntity<Map<String, String>> removeCompanyBanner(@PathVariable Long companyId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authorized");
        }

        Company company = companyService.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        if (!company.getAdmins().contains(currentUser)) {
            throw new AccessDeniedException("Not authorized to remove banner for this company");
        }
        
        try {
            companyService.removeCompanyBanner(companyId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Banner removed successfully",
                "companyId", companyId.toString()
            ));
            
        } catch (Exception e) {
            log.error("Error removing banner for company {}: {}", companyId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to remove banner: " + e.getMessage()));
        }
    }
    
    
  @GetMapping("/{companyUniqueId}/employees")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
@Operation(summary = "Get company employees")
public ResponseEntity<List<EmployerProfileDTO>> getCompanyEmployees(
        @PathVariable String companyUniqueId) {

    User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null) {
        throw new AccessDeniedException("Not authorized");
    }

    Company company = companyService.findByCompanyUniqueId(companyUniqueId)
            .orElseThrow(() -> new ResourceNotFoundException(errMsg + companyUniqueId));

    // Check if the current user is an admin of the company
    if (!company.getAdmins().contains(currentUser)) {
        throw new AccessDeniedException("Not authorized to view employees for this company");
    }
    
    List<EmployerProfileDTO> employees = companyService.getCompanyEmployees(companyUniqueId);
    return ResponseEntity.ok(employees);
}
 @GetMapping("dashboard")
 @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
 @Operation(summary = "Get company dashboard statistics")
    public ResponseEntity<CompanyDashboardStatsDTO> getCompanyDashboardStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Default to last 30 days if not specified
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        return ResponseEntity.ok(dashboardService.getCompanyDashboardStats(startDate, endDate));
    }
   

}
