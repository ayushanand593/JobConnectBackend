package com.DcoDe.jobconnect.services;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.DcoDe.jobconnect.dto.JwtResponseDTO;
import com.DcoDe.jobconnect.dto.UserDTO;
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.repositories.UserRepository;
import com.DcoDe.jobconnect.services.interfaces.AuthServiceI;
import com.DcoDe.jobconnect.utils.CustomUserDetails;
import com.DcoDe.jobconnect.utils.JwtUtils;
import com.DcoDe.jobconnect.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthServiceI {

       private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    public User login(String email, String password) {
        // Authenticate and throw exception if authentication fails
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
        );
        
        // If we reach here, authentication was successful
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }
    
    @Override
    public JwtResponseDTO authenticateAndGenerateToken(String email, String password) {
        User user = login(email, password);
        return generateTokenForUser(user);
    }
    
    @Override
    public JwtResponseDTO generateTokenForUser(User user) {

// 1. Build the authorities list from the user’s role
    List<GrantedAuthority> authorities = List.of(
        new SimpleGrantedAuthority("ROLE_" + user.getRole().name()),
        new SimpleGrantedAuthority(user.getRole().name())
    );

        // Load user details for token generation
         // 2. Create a UserDetails instance with those authorities
         UserDetails userDetails = new CustomUserDetails(user, authorities);
        
        // Generate token
        String token = jwtUtils.generateToken(userDetails);
        
        // Create user DTO
        UserDTO userDTO = mapToDto(user);
        
        // Create and return response
        return JwtResponseDTO.builder()
            .token(token)
            .user(userDTO)
            .build();
    }
    
    private UserDTO mapToDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setEmail(user.getEmail());
        dto.setRole(String.valueOf(user.getRole()));

        if (user.getCompany() != null) {
            dto.setCompanyID(user.getCompany().getId());
        }

        return dto;
    }
}       
