package com.DcoDe.jobconnect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.DcoDe.jobconnect.services.interfaces.CompanyImageServiceI;
import com.DcoDe.jobconnect.services.interfaces.CompanyServiceI;
// import com.DcoDe.jobconnect.services.interfaces.FileStorageServiceI;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

     private final CompanyServiceI companyService;
    //  private final FileStorageServiceI fileStorageService;
     private final CompanyImageServiceI companyImageService;

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
    public ResponseEntity<CompanyDetailDTO> registerCompany(@Valid @RequestBody CompanyRegistrationDTO dto) {
        CompanyDetailDTO registeredCompany = companyService.registerCompany(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredCompany);
    }

    @GetMapping("/{companyUniqueId}")
    public ResponseEntity<CompanyDetailDTO> getCompanyProfile(@PathVariable String companyUniqueId) {
        return ResponseEntity.ok(companyService.getCompanyByUniqueId(companyUniqueId));
    }

     @PostMapping("/{companyUniqueId}/logo")
     @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<ImageUploadResponseDTO> uploadCompanyLogo(
            @PathVariable String companyUniqueId,
            @RequestParam("file") MultipartFile file) {
            
        String fileId = companyImageService.uploadCompanyLogo(companyUniqueId, file);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ImageUploadResponseDTO(fileId, "Logo uploaded successfully"));
    }
    
    @PostMapping("/{companyUniqueId}/banner")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<ImageUploadResponseDTO> uploadCompanyBanner(
            @PathVariable String companyUniqueId,
            @RequestParam("file") MultipartFile file) {
            
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
