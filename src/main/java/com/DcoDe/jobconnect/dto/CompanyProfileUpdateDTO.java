package com.DcoDe.jobconnect.dto;

import lombok.Data;

@Data
public class CompanyProfileUpdateDTO {
    
    private String companyName;


    private String description;

    
    private String website;

    private String industry;
    private String size;
    private String logoUrl;
}
