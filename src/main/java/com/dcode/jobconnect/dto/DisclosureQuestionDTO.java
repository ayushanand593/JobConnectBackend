package com.dcode.jobconnect.dto;

import lombok.Data;

@Data
public class DisclosureQuestionDTO {
    private Long id;
    private String questionText;
    private Boolean isRequired;
}