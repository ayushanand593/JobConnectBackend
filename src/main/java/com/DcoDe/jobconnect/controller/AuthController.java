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
import com.DcoDe.jobconnect.services.interfaces.AuthServiceI;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "API for managing authentication and authorization endpoints")
public class AuthController  {

    private final AuthServiceI authService;

@PostMapping("/login")
@Operation(summary = "Login", description = "Authenticate user and return JWT token")
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
     @Operation(summary = "Update Email", description = "Update user email and require re-login")
    public ResponseEntity<String> updateEmail(@Valid @RequestBody EmailUpdateDTO emailUpdateDTO) {
        authService.updateEmail(emailUpdateDTO);
        return ResponseEntity.ok("Email updated successfully. Please login again.");
    }

    @PutMapping("/update-password")
    @Operation(summary = "Update Password", description = "Update user password and require re-login")
    public ResponseEntity<String> updatePassword(@Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        authService.updatePassword(passwordUpdateDTO);
        return ResponseEntity.ok("Password updated successfully. Please login again.");
    }
}
