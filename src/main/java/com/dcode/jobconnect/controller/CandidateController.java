package com.dcode.jobconnect.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.dcode.jobconnect.dto.CandidateDashboardStatsDTO;
import com.dcode.jobconnect.dto.CandidateProfileDTO;
import com.dcode.jobconnect.dto.CandidateProfileUpdateDTO;
import com.dcode.jobconnect.dto.CandidateRegistrationDTO;
import com.dcode.jobconnect.dto.JobApplicationDTO;
import com.dcode.jobconnect.dto.JobApplicationDetailDTO;
import com.dcode.jobconnect.dto.JwtResponseDTO;
import com.dcode.jobconnect.entities.User;
import com.dcode.jobconnect.enums.ApplicationStatus;
import com.dcode.jobconnect.exceptions.CandidateRegisterException;
import com.dcode.jobconnect.services.interfaces.AuthServiceI;
import com.dcode.jobconnect.services.interfaces.CandidateServiceI;
import com.dcode.jobconnect.services.interfaces.DashboardServiceI;
import com.dcode.jobconnect.services.interfaces.JobApplicationServiceI;
import com.dcode.jobconnect.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/candidate")
@RequiredArgsConstructor
@Tag(name="Candidate", description = "API for managing candidate profiles and job applications.")
public class CandidateController {

        private final CandidateServiceI candidateService;

        private final DashboardServiceI dashboardService;

        private final JobApplicationServiceI jobApplicationService;

         private final AuthServiceI authService;


        @PostMapping("/register")
    @Operation(summary = "Register a new candidate")
    public ResponseEntity<JwtResponseDTO> registerCandidate(
            @Valid @RequestBody CandidateRegistrationDTO dto) {
        
        try {
            // Register the candidate
            candidateService.registerCandidate(dto);
            
            // Auto-login by generating JWT token
            User user = candidateService.findUserByEmail(dto.getEmail());
            JwtResponseDTO authResponse = authService.generateTokenForUser(user);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
        } catch (Exception e) {
           throw new CandidateRegisterException("Registration failed: " + e.getMessage());
        }
    }


    @GetMapping("/profile")
    @Operation(summary = "Get the current candidate's profile")
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
    public ResponseEntity<CandidateProfileDTO> getCurrentProfile() {
           User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        return ResponseEntity.ok(candidateService.getCurrentCandidateProfile());
    }

     @GetMapping("/{id}")
     @Operation(summary = "Get candidate profile by ID")
     @PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE') or hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN') or hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    public ResponseEntity<CandidateProfileDTO> getCandidateById(@PathVariable Long id) {
         User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        // If candidate is viewing, ensure they can only view their own profile
        if (currentUser.getRole().name().equals("CANDIDATE") && !currentUser.getId().equals(id)) {
                throw new AccessDeniedException("You can only view your own profile");
        }
        return ResponseEntity.ok(candidateService.getCandidateById(id));
    }

    @PutMapping("/profile-update")
    @Operation(summary = "Update the current candidate's profile")
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
    public ResponseEntity<CandidateProfileDTO> updateProfile(
            @Valid @RequestBody CandidateProfileUpdateDTO profileDTO) {

         User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        return ResponseEntity.ok(candidateService.updateCandidateProfile(profileDTO));
    }

      @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete candidate profile by ID")
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
    public ResponseEntity<String> deleteCandidate(@PathVariable Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        // If candidate is deleting, ensure they can only delete their own profile
        if (currentUser.getRole().name().equals("CANDIDATE") && !currentUser.getId().equals(id)) {
                throw new AccessDeniedException("You can only delete your own profile");
        }
        
        candidateService.deleteCandidateById(id);
        return ResponseEntity.ok("Candidate deleted successfully.");
    }

@PostMapping(value = "/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@Operation(summary = "Upload resume for the current candidate")
@PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
public ResponseEntity<CandidateProfileDTO> uploadResume(
        @Parameter(
            description = "Resume file to upload", 
            required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        @RequestParam("file") MultipartFile file) {
    User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null) {
        throw new AccessDeniedException("Not authenticated");
    }
     Set<String> allowedContentTypes = Set.of(
        "application/pdf", 
        "application/msword", 
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", 
        "text/plain"
        // If you also want to allow images, add the following:
        // "image/png",
        // "image/jpeg"
    );
    
    if (!allowedContentTypes.contains(file.getContentType())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, 
            "Invalid file type. Only PDF, DOC, DOCX, or TXT files are allowed."
        );
    }
    
    return ResponseEntity.ok(candidateService.uploadResume(file));
}


   @GetMapping("/dashboard")
    @Operation(summary = "Get candidate dashboard statistics")
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
    public ResponseEntity<CandidateDashboardStatsDTO> getCandidateDashboardStats(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }

        // Default to last 30 days if not specified
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        return ResponseEntity.ok(dashboardService.getCandidateDashboardStats(startDate, endDate));
    }

// @GetMapping("/view/applications")
//     @Operation(summary = "Get all job applications for the current candidate")
//     @PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
//     public ResponseEntity<List<JobApplicationDetailDTO>> getCandidateApplications() {
//         User currentUser = SecurityUtils.getCurrentUser();
//         if (currentUser == null) {
//             throw new AccessDeniedException("Not authenticated");
//         }
//         return ResponseEntity.ok(dashboardService.getCandidateApplications());
//     }
 @GetMapping("/my-applications")
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
    public ResponseEntity<Page<JobApplicationDTO>> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(jobApplicationService.getCurrentCandidateApplications(pageable));
    }
    
@GetMapping("/view/applications/{applicationId}")
@Operation(summary = "Get job application details for the current candidate")
@PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
public ResponseEntity<JobApplicationDetailDTO> getCandidateApplicationDetail(@PathVariable Long applicationId) {

     User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        // Call a service method that first verifies this application belongs to the current user
        JobApplicationDetailDTO applicationDetail = dashboardService.getCandidateApplicationDetail(applicationId);

        if (applicationDetail == null) {
            throw new AccessDeniedException("You do not have access to this application");
        }


    return ResponseEntity.ok(applicationDetail);
}

@PatchMapping("/withdraw/applications/{applicationId}")
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
    @Operation(summary = "Withdraw a job application")
    public ResponseEntity<Void> withdrawApplication(@PathVariable Long applicationId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        // Verify that the application belongs to the current candidate
        JobApplicationDTO application = jobApplicationService.getJobApplication(applicationId);
        if (!application.getCandidateId().equals(currentUser.getCandidateProfile().getId())) {
            throw new AccessDeniedException("You can only withdraw your own applications");
        }
        if(application.getStatus().equals(ApplicationStatus.WITHDRAWN)){ 
            throw new AccessDeniedException("This application has already been withdrawn");
        }
        
        jobApplicationService.withdrawApplication(applicationId);
        return ResponseEntity.noContent().build();
    }
}

