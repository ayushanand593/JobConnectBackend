package com.DcoDe.jobconnect.dto;

import com.DcoDe.jobconnect.enums.ApplicationStatus;

import lombok.Data;

@Data
public class JobApplicationUpdateDTO {
    private ApplicationStatus status;
}