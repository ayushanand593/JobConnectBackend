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
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.services.interfaces.CandidateServiceI;
import com.DcoDe.jobconnect.services.interfaces.DashboardServiceI;
import com.DcoDe.jobconnect.services.interfaces.JobApplicationServiceI;
import com.DcoDe.jobconnect.utils.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/candidate")
@RequiredArgsConstructor
public class CandidateController {

        private final CandidateServiceI candidateService;

        private final DashboardServiceI dashboardService;

        private final JobApplicationServiceI jobApplicationService;


          @PostMapping("/register")
    public ResponseEntity<CandidateProfileDTO> registerCandidate(
            @Valid @RequestBody CandidateRegistrationDTO dto) {
        
        // Log the request to help diagnose issues
        System.out.println("Received candidate registration request for: " + dto.getEmail());
        
        try {
            CandidateProfileDTO result = candidateService.registerCandidate(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            // Log the error
            System.err.println("Error registering candidate: " + e.getMessage());
            throw e;
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
    public ResponseEntity<CandidateProfileDTO> getCurrentProfile() {
        return ResponseEntity.ok(candidateService.getCurrentCandidateProfile());
    }

     @GetMapping("/{id}")
    public ResponseEntity<CandidateProfileDTO> getCandidateById(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getCandidateById(id));
    }

    @PutMapping("/profile-update")
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
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
    public ResponseEntity<String> deleteCandidate(@PathVariable Long id) {
        candidateService.deleteCandidateById(id);
        return ResponseEntity.ok("Candidate deleted successfully.");
    }

     @PostMapping("/resume")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<CandidateProfileDTO> uploadResume(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(candidateService.uploadResume(file));
    }
     @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
public ResponseEntity<CandidateDashboardStatsDTO> getCandidateDashboardStats(
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

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
@PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
public ResponseEntity<List<JobApplicationDetailDTO>> getCandidateApplications() {
    return ResponseEntity.ok(dashboardService.getCandidateApplications());
}
@GetMapping("/view/applications/{applicationId}")
@PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
public ResponseEntity<JobApplicationDetailDTO> getCandidateApplicationDetail(@PathVariable Long applicationId) {
    return ResponseEntity.ok(dashboardService.getCandidateApplicationDetail(applicationId));
}

@DeleteMapping("/withdraw/applications/{applicationId}")
@PreAuthorize("hasAuthority('ROLE_CANDIDATE') or hasAuthority('CANDIDATE')")
public ResponseEntity<String> withdrawApplication(@PathVariable Long applicationId) {
   jobApplicationService.withdrawApplication(applicationId);
     return ResponseEntity.ok("Job Application deleted successfully.");
}
}
