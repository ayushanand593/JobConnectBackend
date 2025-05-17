package com.DcoDe.jobconnect.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.server.ResponseStatusException;

import com.DcoDe.jobconnect.dto.EmployeeRegistrationDTO;
import com.DcoDe.jobconnect.dto.EmployerDashboardStatsDTO;
import com.DcoDe.jobconnect.dto.EmployerProfileDTO;
import com.DcoDe.jobconnect.dto.EmployerProfileUpdateDTO;
import com.DcoDe.jobconnect.dto.JobApplicationDTO;
import com.DcoDe.jobconnect.dto.JobDTO;
import com.DcoDe.jobconnect.dto.JwtResponseDTO;
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.enums.ApplicationStatus;
import com.DcoDe.jobconnect.enums.JobStatus;
import com.DcoDe.jobconnect.enums.UserRole;
import com.DcoDe.jobconnect.services.interfaces.AuthServiceI;
import com.DcoDe.jobconnect.services.interfaces.CompanyServiceI;
import com.DcoDe.jobconnect.services.interfaces.DashboardServiceI;
import com.DcoDe.jobconnect.services.interfaces.EmployeeServiceI;
import com.DcoDe.jobconnect.services.interfaces.JobApplicationServiceI;
import com.DcoDe.jobconnect.services.interfaces.JobServiceI;
import com.DcoDe.jobconnect.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employer")
@RequiredArgsConstructor
@Tag(name = "Employer", description = "API for managing employer profiles and job applications.")
public class EmployerController {

    @Autowired
     private final CompanyServiceI companyService;

    @Autowired
    private final EmployeeServiceI employerService;

    @Autowired
    private final JobApplicationServiceI applicationService;

    @Autowired
    private final DashboardServiceI dashboardService;

    @Autowired
    private final JobServiceI  jobService;

    @Autowired
     private final AuthServiceI authService;
        
   @PostMapping("/register")
@Operation(summary = "Register a new employer")
public ResponseEntity<JwtResponseDTO> joinCompany(@Valid @RequestBody EmployeeRegistrationDTO dto) {
    companyService.findByCompanyUniqueId(dto.getCompanyUniqueId())
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Company not found with ID: " + dto.getCompanyUniqueId()
            ));
    
    // Register employer
    Object result = companyService.addEmployerToCompany(dto);
    
    // Auto-login the employer
    User user = companyService.findEmployerByEmail(dto.getEmail());
    JwtResponseDTO authResponse = authService.generateTokenForUser(user);
    
    return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
}

    @GetMapping("/my-profile")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    @Operation(summary = "Get the current employer's profile")
    public ResponseEntity<EmployerProfileDTO> getMyProfile() {
        // Get the current authenticated user
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }

        // Verify the user is an employer
        if (!currentUser.getRole().equals(UserRole.EMPLOYER)) {
            throw new AccessDeniedException("Not authorized to access employer profile");
        }

        // Call the service method that already exists for getting the current user's profile
        EmployerProfileDTO profile = employerService.getCurrentEmployerProfile();
        return ResponseEntity.ok(profile);
    }

     @PutMapping("/profile-update")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    @Operation(summary = "Update the current employer's profile")
    public ResponseEntity<EmployerProfileDTO> updateProfile(
            @Valid @RequestBody EmployerProfileUpdateDTO dto) {
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }

        EmployerProfileDTO updatedProfile = employerService.updateProfile(dto);
       return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping("/delete/{employerId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    @Operation(summary = "Delete employer profile by ID")
    public ResponseEntity<String> deleteEmployer(@PathVariable Long employerId) {
    employerService.deleteEmployerById(employerId);
    return ResponseEntity.ok("Employer deleted successfully.");
}

@GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    @Operation(summary = "Get employer dashboard statistics")
    public ResponseEntity<EmployerDashboardStatsDTO> getEmployerDashboardStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Default to last 30 days if not specified
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        return ResponseEntity.ok(dashboardService.getEmployerDashboardStats(startDate, endDate));
    }

   @GetMapping("/my-jobs")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    @Operation(summary = "Get all jobs posted by the current employer")
public ResponseEntity<List<JobDTO>> getMyJobs() {
    User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null) {
        throw new AccessDeniedException("Not authenticated");
    }

    List<JobDTO> jobs = employerService.getJobsByEmployerId(currentUser.getId());
    return ResponseEntity.ok(jobs);
}
 @GetMapping("/applications/{jobId}")
@PreAuthorize("hasRole('EMPLOYER')")
@Operation(summary = "Get all job applications for a specific job")
public ResponseEntity<List<JobApplicationDTO>> getApplicationsForJob(@PathVariable String jobId) {
    List<JobApplicationDTO> applications = applicationService.getApplicationsForJob(jobId);
    return ResponseEntity.ok(applications);
}

 @PatchMapping("/jobId/{jobId}/status")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Change the status of a job by job ID")
    public ResponseEntity<Void> changeJobStatusByJobId(
            @PathVariable String jobId,
            @RequestParam JobStatus status) {
        jobService.changeJobStatusByJobId(jobId, status);
        return ResponseEntity.ok().build();
    }

//     @PatchMapping("/application/{id}/status")
// public ResponseEntity<Void> updateApplicationStatus(
//         @PathVariable Long id,
//         @RequestBody Map<String, String> payload) {
    
//     // Parse the status from the request body
//     String statusStr = payload.get("status");
//     if (statusStr == null) {
//         throw new IllegalArgumentException("Status is required");
//     }
    
//     ApplicationStatus status;
//     try {
//         status = ApplicationStatus.valueOf(statusStr);
//     } catch (IllegalArgumentException e) {
//         throw new IllegalArgumentException("Invalid status value: " + statusStr);
//     }
    
//     applicationService.updateApplicationStatus(id, status);
//     return ResponseEntity.ok().build();
// }
@PatchMapping("/application/{id}/status")
@PreAuthorize("hasRole('EMPLOYER')")
@Operation(summary = "Update the status of a job application")
public ResponseEntity<Void> updateApplicationStatus(
        @PathVariable Long id,
        @RequestParam ApplicationStatus status) {
    
    applicationService.updateApplicationStatus(id, status);
    return ResponseEntity.ok().build();
}



}
