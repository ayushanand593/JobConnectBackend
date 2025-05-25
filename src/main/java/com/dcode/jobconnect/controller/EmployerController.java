package com.dcode.jobconnect.controller;

import java.time.LocalDate;
import java.util.List;

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

import com.dcode.jobconnect.dto.EmployeeRegistrationDTO;
import com.dcode.jobconnect.dto.EmployerDashboardStatsDTO;
import com.dcode.jobconnect.dto.EmployerProfileDTO;
import com.dcode.jobconnect.dto.EmployerProfileUpdateDTO;
import com.dcode.jobconnect.dto.JobApplicationDTO;
import com.dcode.jobconnect.dto.JobDTO;
import com.dcode.jobconnect.dto.JwtResponseDTO;
import com.dcode.jobconnect.entities.Job;
import com.dcode.jobconnect.entities.User;
import com.dcode.jobconnect.enums.ApplicationStatus;
import com.dcode.jobconnect.enums.JobStatus;
import com.dcode.jobconnect.enums.UserRole;
import com.dcode.jobconnect.services.interfaces.AuthServiceI;
import com.dcode.jobconnect.services.interfaces.CompanyServiceI;
import com.dcode.jobconnect.services.interfaces.DashboardServiceI;
import com.dcode.jobconnect.services.interfaces.EmployeeServiceI;
import com.dcode.jobconnect.services.interfaces.JobApplicationServiceI;
import com.dcode.jobconnect.services.interfaces.JobServiceI;
import com.dcode.jobconnect.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employer")
@RequiredArgsConstructor
@Tag(name = "Employer", description = "API for managing employer profiles and job applications.")
public class EmployerController {

   
     private final CompanyServiceI companyService;

    
    private final EmployeeServiceI employerService;

    
    private final JobApplicationServiceI applicationService;

  
    private final DashboardServiceI dashboardService;

   
    private final JobServiceI  jobService;

   
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
        companyService.addEmployerToCompany(dto);
        
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

    @GetMapping("/{employerId}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER') or hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    @Operation(summary = "Get employer profile by ID")
    public ResponseEntity<EmployerProfileDTO> getEmployerById(@PathVariable Long employerId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        // If employer is viewing, ensure they can only view their own profile
        // unless they're an admin
        if (currentUser.getRole().equals(UserRole.EMPLOYER) && !currentUser.getId().equals(employerId)) {
            throw new AccessDeniedException("You can only view your own profile");
        }
        
        return ResponseEntity.ok(employerService.getEmployerById(employerId));
    }

      @PutMapping("/profile-update")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    @Operation(summary = "Update the current employer's profile")
    public ResponseEntity<EmployerProfileDTO> updateProfile(
            @Valid @RequestBody EmployerProfileUpdateDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }

        EmployerProfileDTO updatedProfile = employerService.updateProfile(dto);
        return ResponseEntity.ok(updatedProfile);
    }

     @DeleteMapping("/delete/{employerId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN') or hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    @Operation(summary = "Delete employer profile by ID")
    public ResponseEntity<String> deleteEmployer(@PathVariable Long employerId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        // If employer is deleting (not admin), ensure they can only delete their own profile
        if (currentUser.getRole().equals(UserRole.EMPLOYER) && !currentUser.getId().equals(employerId)) {
            throw new AccessDeniedException("You can only delete your own profile");
        }
        
        employerService.deleteEmployerById(employerId);
        return ResponseEntity.ok("Employer deleted successfully.");
    }

  @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    @Operation(summary = "Get employer dashboard statistics")
    public ResponseEntity<EmployerDashboardStatsDTO> getEmployerDashboardStats(
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
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    @Operation(summary = "Get all job applications for a specific job")
    public ResponseEntity<List<JobApplicationDTO>> getApplicationsForJob(@PathVariable String jobId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        // Verify that the job belongs to the current employer
        Job job = jobService.getJobEntityByJobId(jobId);
        if (job == null || !job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only view applications for your own jobs");
        }
        
        List<JobApplicationDTO> applications = applicationService.getApplicationsForJob(jobId);
        return ResponseEntity.ok(applications);
    }

  @PatchMapping("/jobId/{jobId}/status")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    @Operation(summary = "Change the status of a job by job ID")
    public ResponseEntity<Void> changeJobStatusByJobId(
            @PathVariable String jobId,
            @RequestParam JobStatus status) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        // Verify that the job belongs to the current employer
        Job job = jobService.getJobEntityByJobId(jobId);
        if (job == null || !job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only change the status of your own jobs");
        }
        
        jobService.changeJobStatusByJobId(jobId, status);
        return ResponseEntity.ok().build();
    }
@PatchMapping("/application/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    @Operation(summary = "Update the status of a job application")
    public ResponseEntity<Void> updateApplicationStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus status) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        // This will need a service method to verify if the application belongs to a job owned by this employer
        if (!applicationService.isApplicationForEmployerJob(id, currentUser.getId())) {
            throw new AccessDeniedException("You can only update applications for your own jobs");
        }
        
        applicationService.updateApplicationStatus(id, status);
        return ResponseEntity.ok().build();
    }
}
