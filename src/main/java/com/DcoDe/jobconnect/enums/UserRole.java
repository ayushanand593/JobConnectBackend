package com.DcoDe.jobconnect.enums;

import java.util.Optional;

import com.DcoDe.jobconnect.entities.User;

public enum UserRole {
    CANDIDATE, EMPLOYER, ADMIN;

    public Optional<User> stream() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stream'");
    }

    Optional<User> map(Object object) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'map'");
    }
}
