package com.DcoDe.jobconnect.services.interfaces;

import com.DcoDe.jobconnect.dto.EmailUpdateDTO;
import com.DcoDe.jobconnect.dto.JwtResponseDTO;
import com.DcoDe.jobconnect.dto.PasswordUpdateDTO;
import com.DcoDe.jobconnect.entities.User;

public interface AuthServiceI {
    User login(String email, String password, Boolean termsAccepted);
    JwtResponseDTO authenticateAndGenerateToken(String email, String password, Boolean termsAccepted);
    JwtResponseDTO generateTokenForUser(User user);
        void updateEmail(EmailUpdateDTO emailUpdateDTO);
    void updatePassword(PasswordUpdateDTO passwordUpdateDTO);
}

