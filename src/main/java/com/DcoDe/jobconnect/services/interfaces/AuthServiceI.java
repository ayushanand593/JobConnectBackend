package com.DcoDe.jobconnect.services.interfaces;

import com.DcoDe.jobconnect.dto.JwtResponseDTO;
import com.DcoDe.jobconnect.entities.User;

public interface AuthServiceI {
    User login(String email, String password);
    JwtResponseDTO authenticateAndGenerateToken(String email, String password);
    JwtResponseDTO generateTokenForUser(User user);
}

