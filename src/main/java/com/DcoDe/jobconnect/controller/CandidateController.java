package com.DcoDe.jobconnect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.DcoDe.jobconnect.dto.CandidateProfileDTO;
import com.DcoDe.jobconnect.dto.CandidateRegistrationDTO;
import com.DcoDe.jobconnect.services.interfaces.CandidateServiceI;

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
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<CandidateProfileDTO> getCurrentProfile() {
        return ResponseEntity.ok(candidateService.getCurrentCandidateProfile());
    }
}
