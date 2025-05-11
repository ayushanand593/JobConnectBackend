package com.DcoDe.jobconnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.DcoDe.jobconnect.dto.LoginDTO;
import com.DcoDe.jobconnect.dto.UserDTO;
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.services.interfaces.AuthServiceI;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController  {

    private final AuthServiceI authService;

     @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        User user = authService.login(loginDTO.getEmail(), loginDTO.getPassword());
        UserDTO response = mapToDto(user);
        return ResponseEntity.ok(response);
    }
    private UserDTO mapToDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setEmail(user.getEmail());
        dto.setRole(String.valueOf(user.getRole()));

        // Assuming user has a getCompany() that returns a Company object or null
        if (user.getCompany() != null) {
            dto.setCompanyID(user.getCompany().getId());
        }

        return dto;
    }
}
