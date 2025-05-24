package com.dcode.jobconnect.services.interfaces;

import com.dcode.jobconnect.dto.EmailUpdateDTO;
import com.dcode.jobconnect.dto.JwtResponseDTO;
import com.dcode.jobconnect.dto.PasswordUpdateDTO;
import com.dcode.jobconnect.entities.User;

public interface AuthServiceI {
    User login(String email, String password, Boolean termsAccepted);
    JwtResponseDTO authenticateAndGenerateToken(String email, String password, Boolean termsAccepted);
    JwtResponseDTO generateTokenForUser(User user);
        void updateEmail(EmailUpdateDTO emailUpdateDTO);
    void updatePassword(PasswordUpdateDTO passwordUpdateDTO);
}

