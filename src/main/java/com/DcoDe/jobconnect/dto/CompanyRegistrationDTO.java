package com.DcoDe.jobconnect.dto;



import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyRegistrationDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Company ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Company ID can only contain letters, numbers, hyphens, and underscores")
    private String companyUniqueId;

    private String industry;
    private String size;
    private String website;
    private String description;
}

