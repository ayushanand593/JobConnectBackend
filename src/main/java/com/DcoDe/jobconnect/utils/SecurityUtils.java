package com.DcoDe.jobconnect.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.DcoDe.jobconnect.entities.User;

public class SecurityUtils {

    public static User getCurrentUser() {
     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        System.out.println("Authentication: " + (authentication != null ? authentication.getName() : "null"));
        System.out.println("Authentication principal: " + 
            (authentication != null ? authentication.getPrincipal().getClass().getName() : "null"));
        
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getUser();
            } else {
                System.out.println("Principal is not CustomUserDetails: " + principal.getClass().getName());
            }
        }
        
        return null;
    }
}
