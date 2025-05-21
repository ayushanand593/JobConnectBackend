package com.DcoDe.jobconnect.services.interfaces;

import com.DcoDe.jobconnect.entities.Company;
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.dto.*;

import java.util.List;
import java.util.Optional;

public interface CompanyServiceI {
 CompanyDetailDTO registerCompany(CompanyRegistrationDTO dto);
    Optional<Company> findByCompanyUniqueId(String companyUniqueId);
    CompanyDetailDTO getCompanyByUniqueId(String companyUniqueId);
  
    EmployerProfileDTO addEmployerToCompany(EmployeeRegistrationDTO dto);

    User findCompanyAdminByEmail(String email);

    User findEmployerByEmail(String email);

    List<EmployerProfileDTO> getCompanyEmployees(String companyUniqueId);

    boolean isCompanyAdmin(User user);

    CompanyDetailDTO updateCompanyProfile(CompanyProfileUpdateDTO profileDTO);

    // void deleteCompany(Company company);

     void deleteCompanyById(String companyUniqueId);
}
