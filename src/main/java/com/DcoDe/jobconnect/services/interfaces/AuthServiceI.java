package com.DcoDe.jobconnect.services.interfaces;

import com.DcoDe.jobconnect.entities.User;

public interface AuthServiceI {
    User login(String email, String password);
    User getCurrentUser();
}
