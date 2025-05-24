package com.dcode.jobconnect.dto;

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
    private String location;
private String aboutUs;
private String benefits;
    private String logoUrl;
    private String bannerUrl;
    private LocalDateTime createdAt;
}
