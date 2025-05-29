package com.dcode.jobconnect.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private String timeStamp="timestamp";
    private String status="status";
    private String error="error";
    private String message="message";
    private String path="path";
    // Handle specific exceptions from CandidateService
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(timeStamp, LocalDateTime.now());
        body.put(status, HttpStatus.FORBIDDEN.value());
        body.put(error, HttpStatus.FORBIDDEN.getReasonPhrase());
        body.put(message, ex.getMessage());
        body.put(path, request.getDescription(false).replace("uri=", ""));
        
        log.error("Access denied: {}", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(timeStamp, LocalDateTime.now());
        body.put(status, HttpStatus.NOT_FOUND.value());
        body.put(error, HttpStatus.NOT_FOUND.getReasonPhrase());
        body.put(message, ex.getMessage());
        body.put(path, request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(timeStamp, LocalDateTime.now());
        body.put(status, HttpStatus.BAD_REQUEST.value());
        body.put(error, HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put(message, ex.getMessage());
        body.put(path, request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Custom exception for duplicate email
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Object> handleDuplicateEmailException(DuplicateEmailException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(timeStamp, LocalDateTime.now());
        body.put(status, HttpStatus.CONFLICT.value());
        body.put(error, HttpStatus.CONFLICT.getReasonPhrase());
        body.put(message, ex.getMessage());
        body.put(path, request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // Handling file-related exceptions
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<Object> handleFileUploadException(FileUploadException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(timeStamp, LocalDateTime.now());
        body.put(status, HttpStatus.BAD_REQUEST.value());
        body.put(error, HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put(message, ex.getMessage());
        body.put(path, request.getDescription(false).replace("uri=", ""));
        
        log.error("File upload error: {}", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

     // Handling file not found exception
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<Object> handleFileNotFoundException(FileNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(timeStamp, LocalDateTime.now());
        body.put(status, HttpStatus.NOT_FOUND.value());
        body.put(error, HttpStatus.NOT_FOUND.getReasonPhrase());
        body.put(message, ex.getMessage());
        body.put(path, request.getDescription(false).replace("uri=", ""));
        
        log.error("File not found: {}", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // Handle the JobSearchException from your existing handler
    @ExceptionHandler(JobSearchException.class)
    public ResponseEntity<Object> handleJobSearchException(JobSearchException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(timeStamp, LocalDateTime.now());
        body.put(status, ex.getStatus().value());
        body.put(error, ex.getStatus().getReasonPhrase());
        body.put(message, ex.getMessage());
        body.put(path, request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, ex.getStatus());
    }

    // Handle ResponseStatusException
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(timeStamp, LocalDateTime.now());
        body.put(status, ex.getStatusCode().value());
        body.put(error, ex.getStatusCode().toString());
        body.put(message, ex.getReason());
        body.put(path, request.getDescription(false).replace("uri=", ""));
        
        log.error("Response status exception: {}", ex.getMessage());
        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    // Handle UsernameNotFoundException
     @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(timeStamp, LocalDateTime.now());
        body.put(status, HttpStatus.UNAUTHORIZED.value());
        body.put(error, HttpStatus.UNAUTHORIZED.getReasonPhrase());
        body.put(message, ex.getMessage());
        body.put(path, request.getDescription(false).replace("uri=", ""));
        
        log.error("Authentication failed: {}", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

// Handle CompanyNotFoundException
     @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<Object> handleCompanyNotFoundException(CompanyNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(timeStamp, LocalDateTime.now());
        body.put(status, HttpStatus.UNAUTHORIZED.value());
        body.put(error, HttpStatus.UNAUTHORIZED.getReasonPhrase());
        body.put(message, ex.getMessage());
        body.put(path, request.getDescription(false).replace("uri=", ""));
        
        log.error("Company not found", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    // Handle InvalidImageException
     @ExceptionHandler(InvalidImageException.class)
    public ResponseEntity<Object> handleInvalidImageException(InvalidImageException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(timeStamp, LocalDateTime.now());
        body.put(status, HttpStatus.UNAUTHORIZED.value());
        body.put(error, HttpStatus.UNAUTHORIZED.getReasonPhrase());
        body.put(message, ex.getMessage());
        body.put(path, request.getDescription(false).replace("uri=", ""));
        
        log.error("Invalid image", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

       // Handle TermsNotAcceptedException
     @ExceptionHandler(TermsNotAcceptedException.class)
    public ResponseEntity<Object> handleTermsNotAcceptedException(TermsNotAcceptedException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(timeStamp, LocalDateTime.now());
        body.put(status, HttpStatus.UNAUTHORIZED.value());
        body.put(error, HttpStatus.UNAUTHORIZED.getReasonPhrase());
        body.put(message, ex.getMessage());
        body.put(path, request.getDescription(false).replace("uri=", ""));
        
        log.error("terms not accepted", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    

       // Handle CandidateRegisterException
     @ExceptionHandler(CandidateRegisterException.class)
    public ResponseEntity<Object> handleCandidateRegisterException(CandidateRegisterException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(timeStamp, LocalDateTime.now());
        body.put(status, HttpStatus.CONFLICT.value());
        body.put(error, HttpStatus.CONFLICT.getReasonPhrase());
        body.put(message, ex.getMessage());
        body.put(path, request.getDescription(false).replace("uri=", ""));
        
        log.error(ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // Default exception handler for any other RuntimeExceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(timeStamp, LocalDateTime.now());
        body.put(status, HttpStatus.BAD_REQUEST.value());
        body.put(error, HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put(message, ex.getMessage());
        body.put(path, request.getDescription(false).replace("uri=", ""));
        
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    
    // General fallback exception handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(timeStamp, LocalDateTime.now());
        body.put(status, HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put(error, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        body.put(message, "An unexpected error occurred");
        body.put(path, request.getDescription(false).replace("uri=", ""));

        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}