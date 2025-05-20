package com.DcoDe.jobconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedJobDTO {
    private Long id;
    private Long candidateId;
    private String jobId;
    private String jobTitle;
    private String companyName;
    private LocalDateTime savedAt;
}