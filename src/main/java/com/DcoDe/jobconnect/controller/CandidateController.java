package com.DcoDe.jobconnect.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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

import com.DcoDe.jobconnect.dto.CandidateDashboardStatsDTO;
import com.DcoDe.jobconnect.dto.CandidateProfileDTO;
import com.DcoDe.jobconnect.dto.CandidateProfileUpdateDTO;
import com.DcoDe.jobconnect.dto.CandidateRegistrationDTO;
import com.DcoDe.jobconnect.dto.JobApplicationDetailDTO;
import com.DcoDe.jobconnect.dto.JwtResponseDTO;
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.services.interfaces.AuthServiceI;
import com.DcoDe.jobconnect.services.interfaces.CandidateServiceI;
import com.DcoDe.jobconnect.services.interfaces.DashboardServiceI;
import com.DcoDe.jobconnect.services.interfaces.JobApplicationServiceI;
import com.DcoDe.jobconnect.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
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
        
        System.out.println("Received candidate registration request for: " + dto.getEmail());
        
        try {
            // Register the candidate
            candidateService.registerCandidate(dto);
            
            // Auto-login by generating JWT token
            User user = candidateService.findUserByEmail(dto.getEmail());
            JwtResponseDTO authResponse = authService.generateTokenForUser(user);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
        } catch (Exception e) {
            System.err.println("Error registering candidate: " + e.getMessage());
            throw e;
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
        if (currentUser.getRole().name().equals("CANDIDATE")) {
            if (!currentUser.getId().equals(id)) {
                throw new AccessDeniedException("You can only view your own profile");
            }
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
        if (currentUser.getRole().name().equals("CANDIDATE")) {
            if (!currentUser.getId().equals(id)) {
                throw new AccessDeniedException("You can only delete your own profile");
            }
        }
        
        candidateService.deleteCandidateById(id);
        return ResponseEntity.ok("Candidate deleted successfully.");
    }

     @PostMapping("/resume")
    @Operation(summary = "Upload resume for the current candidate")
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
    public ResponseEntity<CandidateProfileDTO> uploadResume(
            @RequestParam("file") MultipartFile file) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
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

@GetMapping("/view/applications")
    @Operation(summary = "Get all job applications for the current candidate")
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
    public ResponseEntity<List<JobApplicationDetailDTO>> getCandidateApplications() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        return ResponseEntity.ok(dashboardService.getCandidateApplications());
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

 @DeleteMapping("/withdraw/applications/{applicationId}")
    @Operation(summary = "Withdraw a job application for the current candidate")
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
    public ResponseEntity<String> withdrawApplication(@PathVariable Long applicationId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        // Service should verify the application belongs to current user before withdrawal
        jobApplicationService.withdrawApplication(applicationId);
        return ResponseEntity.ok("Job Application withdrawn successfully.");
    }
}

