package com.dcode.jobconnect.exceptions;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(String message) {
        super(message);
    }
    
    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
