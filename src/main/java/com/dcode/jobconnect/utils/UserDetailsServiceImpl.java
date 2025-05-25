package com.dcode.jobconnect.utils;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.dcode.jobconnect.entities.User;
import com.dcode.jobconnect.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
     
    private final UserRepository userRepository;
   

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
         log.debug("Attempting to load user: {}", username);
        
        // Find user by email/username
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                      log.warn("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
        
         log.debug("User found: {}, roles: {}", user.getEmail(), user.getRole());
        
        // Use your CustomUserDetails implementation
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add both formats of the role
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        authorities.add(new SimpleGrantedAuthority(user.getRole().name()));
        
        return new CustomUserDetails(user, authorities);
    }
      
}
