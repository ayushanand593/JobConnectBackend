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
    private String bannerUrl;
    private LocalDateTime createdAt;

    //  public void setLogoFileId(String logoFileId) {
    //     if (logoFileId != null) {
    //         this.logoUrl = "/api/companies/images/" + logoFileId;
    //     }
    // }
    
    // public void setBannerFileId(String bannerFileId) {
    //     if (bannerFileId != null) {
    //         this.bannerUrl = "/api/companies/images/" + bannerFileId;
    //     }
    // }
}
