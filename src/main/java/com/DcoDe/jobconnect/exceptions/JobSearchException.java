package com.DcoDe.jobconnect.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class JobSearchException extends RuntimeException {
    private final HttpStatus status;

    public JobSearchException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public JobSearchException(String message) {
        this(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}