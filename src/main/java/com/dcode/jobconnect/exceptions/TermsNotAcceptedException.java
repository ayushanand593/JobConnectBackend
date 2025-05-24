package com.dcode.jobconnect.exceptions;

public class TermsNotAcceptedException extends RuntimeException {
    public TermsNotAcceptedException(String message) {
        super(message);
    }
}