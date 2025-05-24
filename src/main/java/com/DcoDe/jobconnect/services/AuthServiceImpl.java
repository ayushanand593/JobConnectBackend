package com.dcode.jobconnect.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dcode.jobconnect.dto.EmailUpdateDTO;
import com.dcode.jobconnect.dto.JwtResponseDTO;
import com.dcode.jobconnect.dto.PasswordUpdateDTO;
import com.dcode.jobconnect.dto.UserDTO;
import com.dcode.jobconnect.entities.User;
import com.dcode.jobconnect.exceptions.TermsNotAcceptedException;
import com.dcode.jobconnect.repositories.UserRepository;
import com.dcode.jobconnect.services.interfaces.AuthServiceI;
import com.dcode.jobconnect.utils.CustomUserDetails;
import com.dcode.jobconnect.utils.JwtUtils;
import com.dcode.jobconnect.utils.SecurityUtils;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthServiceI {

       private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

@Override
public User login(String email, String password, Boolean termsAccepted) {
    // Authenticate first
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(email, password)
    );
    
    // Get the authenticated user
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    
    // Check if user has already accepted terms
    if (user.getTermsAccepted() != null && user.getTermsAccepted()) {
        // User has already accepted terms, no need to check again
        return user;
    }
    
    // User hasn't accepted terms yet - require acceptance
    if (termsAccepted == null || !termsAccepted) {
        throw new TermsNotAcceptedException("You must accept the terms and conditions to continue");
    }
    
    // First-time acceptance - update user record
    user.setTermsAccepted(true);
    user.setTermsAcceptedAt(LocalDateTime.now());
    userRepository.save(user);
    
    return user;
}
    
    @Override
public JwtResponseDTO authenticateAndGenerateToken(String email, String password, Boolean termsAccepted) {
    User user = login(email, password, termsAccepted);
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
     @Override
    public void updateEmail(EmailUpdateDTO emailUpdateDTO) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("Not authenticated");
        }

        // Verify current password
        if (!passwordEncoder.matches(emailUpdateDTO.getCurrentPassword(), currentUser.getPassword())) {
            throw new BadCredentialsException("Invalid current password");
        }

        // Update email
        currentUser.setEmail(emailUpdateDTO.getNewEmail());
        userRepository.save(currentUser);
    }

    @Override
    public void updatePassword(PasswordUpdateDTO passwordUpdateDTO) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("Not authenticated");
        }

        // Verify current password
        if (!passwordEncoder.matches(passwordUpdateDTO.getCurrentPassword(), currentUser.getPassword())) {
            throw new BadCredentialsException("Invalid current password");
        }

        // Update password
        String encodedNewPassword = passwordEncoder.encode(passwordUpdateDTO.getNewPassword());
        currentUser.setPassword(encodedNewPassword);
        userRepository.save(currentUser);
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
