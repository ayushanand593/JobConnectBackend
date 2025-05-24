package com.dcode.jobconnect.dto;

import com.dcode.jobconnect.enums.ApplicationStatus;

import lombok.Data;

@Data
public class JobApplicationUpdateDTO {
    private ApplicationStatus status;
}