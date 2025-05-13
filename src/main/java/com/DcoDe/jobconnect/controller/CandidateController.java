package com.DcoDe.jobconnect.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.DcoDe.jobconnect.dto.CandidateProfileDTO;
import com.DcoDe.jobconnect.dto.CandidateProfileUpdateDTO;
import com.DcoDe.jobconnect.dto.CandidateRegistrationDTO;
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.services.interfaces.CandidateServiceI;
import com.DcoDe.jobconnect.utils.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/candidate")
@RequiredArgsConstructor
public class CandidateController {

        private final CandidateServiceI candidateService;


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
    public ResponseEntity<?> deleteCandidate(@PathVariable Long id) {
        candidateService.deleteCandidateById(id);
        return ResponseEntity.ok("Candidate deleted successfully.");
    }
}
