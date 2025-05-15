package com.DcoDe.jobconnect.utils;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.repositories.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
     
     @Autowired
    private UserRepository userRepository;
    
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Attempting to load user: " + username);
        
        // Find user by email/username
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    System.out.println("User not found: " + username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
        
        System.out.println("User found: " + user.getEmail() + ", roles: " + user.getRole());
        
        // Use your CustomUserDetails implementation
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add both formats of the role
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        authorities.add(new SimpleGrantedAuthority(user.getRole().name()));
        
        return new CustomUserDetails(user, authorities);
    }
      
}
