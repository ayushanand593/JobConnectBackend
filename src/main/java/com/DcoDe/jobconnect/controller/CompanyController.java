package com.DcoDe.jobconnect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.DcoDe.jobconnect.dto.CompanyDetailDTO;
import com.DcoDe.jobconnect.dto.CompanyRegistrationDTO;
import com.DcoDe.jobconnect.dto.ImageUploadResponseDTO;
import com.DcoDe.jobconnect.dto.JwtResponseDTO;
import com.DcoDe.jobconnect.entities.Company;
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.exceptions.ResourceNotFoundException;
import com.DcoDe.jobconnect.services.interfaces.AuthServiceI;
import com.DcoDe.jobconnect.services.interfaces.CompanyImageServiceI;
import com.DcoDe.jobconnect.services.interfaces.CompanyServiceI;
// import com.DcoDe.jobconnect.services.interfaces.FileStorageServiceI;
import com.DcoDe.jobconnect.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name="Company", description = "API for managing company profiles and images.")
public class CompanyController {

     private final CompanyServiceI companyService;
    //  private final FileStorageServiceI fileStorageService;
     private final CompanyImageServiceI companyImageService;

      private final AuthServiceI authService;

    // @PostMapping("/register")
    // public ResponseEntity<CompanyDetailDTO> registerCompany(@Valid @RequestBody CompanyRegistrationDTO dto) {
    //     CompanyDetailDTO registeredCompany = companyService.registerCompany(dto);
    //     return ResponseEntity.status(HttpStatus.CREATED).body(registeredCompany);
    // }

    // @GetMapping("/{companyUniqueId}")
    // public ResponseEntity<CompanyDetailDTO> getCompanyProfile(@PathVariable String companyUniqueId) {
    //     return ResponseEntity.ok(companyService.getCompanyByUniqueId(companyUniqueId));
    // }

  @PostMapping("/register")
@Operation(summary = "Register a new company")
public ResponseEntity<JwtResponseDTO> registerCompany(@Valid @RequestBody CompanyRegistrationDTO dto) {
    CompanyDetailDTO registeredCompany = companyService.registerCompany(dto);
    
    // Auto-login the company admin
    User user = companyService.findCompanyAdminByEmail(dto.getEmail());
    JwtResponseDTO authResponse = authService.generateTokenForUser(user);
    
    return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
}

    @GetMapping("/{companyUniqueId}")
    @Operation(summary = "Get company profile by unique ID")
    public ResponseEntity<CompanyDetailDTO> getCompanyProfile(@PathVariable String companyUniqueId) {
        return ResponseEntity.ok(companyService.getCompanyByUniqueId(companyUniqueId));
    }

     @PostMapping("/{companyUniqueId}/logo")
     @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    @Operation(summary = "Upload company logo")
    public ResponseEntity<ImageUploadResponseDTO> uploadCompanyLogo(
            @PathVariable String companyUniqueId,
            @RequestParam("file") MultipartFile file) {

         User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authorized");
        }

        Company company = companyService.findByCompanyUniqueId(companyUniqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with unique ID: " + companyUniqueId));

        // Check if the current user is an admin of the company
        if (!company.getAdmins().contains(currentUser) ) {
            throw new AccessDeniedException("Not authorized to upload banner for this company");
        }
    
            
        String fileId = companyImageService.uploadCompanyLogo(companyUniqueId, file);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ImageUploadResponseDTO(fileId, "Logo uploaded successfully"));
    }
    
    @PostMapping("/{companyUniqueId}/banner")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    @Operation(summary = "Upload company banner")
    public ResponseEntity<ImageUploadResponseDTO> uploadCompanyBanner(
            @PathVariable String companyUniqueId,
            @RequestParam("file") MultipartFile file) {

          User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authorized");
        }

        Company company = companyService.findByCompanyUniqueId(companyUniqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with unique ID: " + companyUniqueId));

        // Check if the current user is an admin of the company
        if (!company.getAdmins().contains(currentUser) ) {
            throw new AccessDeniedException("Not authorized to upload banner for this company");
        }
            
        String fileId = companyImageService.uploadCompanyBanner(companyUniqueId, file);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ImageUploadResponseDTO(fileId, "Banner uploaded successfully"));
    }
    
    // @GetMapping("/images/{fileId}")
    // public ResponseEntity<byte[]> getImage(@PathVariable String fileId) {
    //     try {
    //         FileDocument fileDocument = fileStorageService.getFile(fileId);
    //         byte[] imageData = fileStorageService.getFileData(fileId);
            
    //         HttpHeaders headers = new HttpHeaders();
    //         headers.setContentType(MediaType.parseMediaType(fileDocument.getContentType()));
    //         headers.setContentLength(imageData.length);
            
    //         return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    //     } catch (RuntimeException e) {
    //         return ResponseEntity.notFound().build();
    //     }
    // }

}
