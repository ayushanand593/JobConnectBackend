package com.dcode.jobconnect.dto;

import lombok.Data;

@Data
public class DisclosureAnswerDTO {
    private Long questionId;
    private String questionText;
    private String answerText;
}