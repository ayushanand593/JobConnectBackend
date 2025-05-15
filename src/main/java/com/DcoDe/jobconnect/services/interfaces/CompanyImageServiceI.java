package com.DcoDe.jobconnect.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface CompanyImageServiceI {
    String uploadCompanyLogo(String companyUniqueId, MultipartFile file);
    String uploadCompanyBanner(String companyUniqueId, MultipartFile file);
}