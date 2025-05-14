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
import com.DcoDe.jobconnect.entities.Company;
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.enums.UserRole;
import com.DcoDe.jobconnect.services.interfaces.CompanyServiceI;
import com.DcoDe.jobconnect.services.interfaces.DashboardServiceI;
import com.DcoDe.jobconnect.services.interfaces.EmployeeServiceI;
import com.DcoDe.jobconnect.services.interfaces.JobApplicationServiceI;
import com.DcoDe.jobconnect.utils.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employer")
@RequiredArgsConstructor
public class EmployerController {

    @Autowired
     private final CompanyServiceI companyService;

    @Autowired
    private final EmployeeServiceI employerService;

    @Autowired
    private final JobApplicationServiceI applicationService;

    @Autowired
    private final DashboardServiceI dashboardService;
        
    @PostMapping("/register")
    public ResponseEntity<?> joinCompany(@Valid @RequestBody EmployeeRegistrationDTO dto) {
        Company company = companyService.findByCompanyUniqueId(dto.getCompanyUniqueId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Company not found with ID: " + dto.getCompanyUniqueId()
                ));
        return ResponseEntity.ok(companyService.addEmployerToCompany(dto));
    }

    @GetMapping("/my-profile")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
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
    public ResponseEntity<?> deleteEmployer(@PathVariable Long employerId) {
    employerService.deleteEmployerById(employerId);
    return ResponseEntity.ok("Employer deleted successfully.");
}

@GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
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
public ResponseEntity<List<JobApplicationDTO>> getApplicationsForJob(@PathVariable String jobId) {
    List<JobApplicationDTO> applications = applicationService.getApplicationsForJob(jobId);
    return ResponseEntity.ok(applications);
}



}
