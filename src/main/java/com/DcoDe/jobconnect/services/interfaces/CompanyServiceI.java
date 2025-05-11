package com.DcoDe.jobconnect.services.interfaces;

import com.DcoDe.jobconnect.dto.CompanyRegistrationDTO;
import com.DcoDe.jobconnect.entities.Company;
import com.DcoDe.jobconnect.dto.*;



import java.util.Map;
import java.util.Optional;

public interface CompanyServiceI {
 CompanyDetailDTO registerCompany(CompanyRegistrationDTO dto);
    Optional<Company> findByCompanyUniqueId(String companyUniqueId);
    CompanyDetailDTO getCompanyByUniqueId(String companyUniqueId);
    // CompanyDetailDTO getCurrentCompanyProfile();
    // CompanyDetailDTO updateCompanyProfile(CompanyProfileUpdateDTO profileDTO);
    EmployerProfileDTO addEmployerToCompany(EmployeeRegistrationDTO dto);
    // boolean isCompanyAdmin(User user, String companyName);
    // Page<CompanyDetailDTO> searchCompanies(String keyword, int page, int size);
}
