package com.DcoDe.jobconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class JobCreateDTO {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Job type is required")
    private String jobType;

    private String experienceLevel;

    @NotBlank(message = "Description is required")
    private String description;

    private String requirements;
    private String responsibilities;
    private String salaryRange;
    private List<String> skills;

    private List<DisclosureQuestionDTO> disclosureQuestions;
}