package com.dcode.jobconnect.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.dcode.jobconnect.entities.User;

public class SecurityUtils {

    private SecurityUtils() {}

    public static User getCurrentUser() {
     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof CustomUserDetails customUserDetails) {
                return customUserDetails.getUser();
            } else {
                throw new IllegalStateException("Principal is not CustomUserDetails: " + principal.getClass().getName());
            }
        }
        
        return null;
    }
}
