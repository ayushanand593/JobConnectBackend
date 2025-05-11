package com.DcoDe.jobconnect.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CompanyDetailDTO {
    private Long id;
    private String companyName;
    private String companyUniqueId;
    private String industry;
    private String size;
    private String website;
    private String description;
    private String logoUrl;
    private LocalDateTime createdAt;
}
