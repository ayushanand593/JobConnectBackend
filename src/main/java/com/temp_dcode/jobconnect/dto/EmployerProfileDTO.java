package com.dcode.jobconnect.dto;

import lombok.Data;

@Data
public class EmployerProfileDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String jobTitle;
    private String companyName;
    private Long companyId;
    private String profilePictureUrl; 
}