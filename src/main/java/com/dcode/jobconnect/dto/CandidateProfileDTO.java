package com.dcode.jobconnect.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CandidateProfileDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String headline;
    private String summary;
    private Integer experienceYears;
    private String resumeFileId;
    private String resumeFileName;
    private List<SkillDTO> skills;
    private LocalDateTime createdAt;
}
