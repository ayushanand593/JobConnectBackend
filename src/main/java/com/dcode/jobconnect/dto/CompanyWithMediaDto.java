package com.dcode.jobconnect.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyWithMediaDto {
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Logo fields
    private String logoFileId;
    private String logoBase64;
    private String logoContentType;
    private String logoFileName;
    private String logoDataUrl;
    
    // Banner fields
    private String bannerFileId;
    private String bannerBase64;
    private String bannerContentType;
    private String bannerFileName;
    private String bannerDataUrl;
}
