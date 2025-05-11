package com.DcoDe.jobconnect.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmployeeRegistrationDTO {
   @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    // @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Company ID is required")
    private String companyUniqueId;
}
