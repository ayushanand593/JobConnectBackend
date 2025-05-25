package com.dcode.jobconnect.services.interfaces;

import java.util.List;
import java.util.Optional;

import com.dcode.jobconnect.dto.*;
import com.dcode.jobconnect.entities.Company;
import com.dcode.jobconnect.entities.User;

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

     void deleteCompanyById(String companyUniqueId);
}
