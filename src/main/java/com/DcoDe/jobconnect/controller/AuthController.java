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
import com.DcoDe.jobconnect.services.interfaces.AuthServiceI;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController  {

    private final AuthServiceI authService;

      @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        JwtResponseDTO response = authService.authenticateAndGenerateToken(
                loginDTO.getEmail(), 
                loginDTO.getPassword()
        );
        return ResponseEntity.ok(response);
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
