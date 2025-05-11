package com.DcoDe.jobconnect.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CandidateProfileUpdateDTO {
    @NotBlank(message = "First name is required")
    private String firstName;

   
    private String lastName;
    private String phone;
    private String headline;
    private String summary;
    private Integer experienceYears;
    private String resumeUrl;
    private List<String> skills;
}
