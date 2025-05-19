package com.DcoDe.jobconnect.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailUpdateDTO {
    @NotBlank(message = "New email is required")
    @Email(message = "Valid email is required")
    private String newEmail;

    @NotBlank(message = "Current password is required")
    private String currentPassword;
}
