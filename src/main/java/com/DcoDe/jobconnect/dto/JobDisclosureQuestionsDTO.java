package com.DcoDe.jobconnect.dto;

import java.util.List;

import lombok.Data;

@Data
public class JobDisclosureQuestionsDTO {
    private String jobId;
    private String jobTitle;
    private String companyName;
    private List<DisclosureQuestionDTO> disclosureQuestions;
}
