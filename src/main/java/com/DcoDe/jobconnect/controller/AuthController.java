package com.DcoDe.jobconnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.DcoDe.jobconnect.dto.EmailUpdateDTO;
import com.DcoDe.jobconnect.dto.JwtResponseDTO;
import com.DcoDe.jobconnect.dto.LoginDTO;
import com.DcoDe.jobconnect.dto.PasswordUpdateDTO;
import com.DcoDe.jobconnect.exceptions.TermsNotAcceptedException;
import com.DcoDe.jobconnect.repositories.UserRepository;
import com.DcoDe.jobconnect.services.interfaces.AuthServiceI;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController  {

    private final AuthServiceI authService;
    private final UserRepository userRepository;

@PostMapping("/login")
public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
    try {
        JwtResponseDTO response = authService.authenticateAndGenerateToken(
            loginDTO.getEmail(), 
            loginDTO.getPassword(), 
            loginDTO.getTermsAccepted()
        );
        return ResponseEntity.ok(response);
    } catch (TermsNotAcceptedException e) {
        // Return specific error for terms not accepted
        return ResponseEntity.badRequest()
            .body(JwtResponseDTO.builder()
                .error("TERMS_NOT_ACCEPTED")
                .message("You must accept the terms and conditions to continue")
                .requiresTermsAcceptance(true)
                .build());
    }
}
     @PutMapping("/update-email")
    public ResponseEntity<String> updateEmail(@Valid @RequestBody EmailUpdateDTO emailUpdateDTO) {
        authService.updateEmail(emailUpdateDTO);
        return ResponseEntity.ok("Email updated successfully. Please login again.");
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        authService.updatePassword(passwordUpdateDTO);
        return ResponseEntity.ok("Password updated successfully. Please login again.");
    }
}
