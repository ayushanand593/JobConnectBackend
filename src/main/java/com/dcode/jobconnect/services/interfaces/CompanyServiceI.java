package com.dcode.jobconnect.services.interfaces;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.dcode.jobconnect.dto.*;
import com.dcode.jobconnect.entities.Company;
import com.dcode.jobconnect.entities.User;

public interface CompanyServiceI {
 CompanyDetailDTO registerCompany(CompanyRegistrationDTO dto);
    Optional<Company> findByCompanyUniqueId(String companyUniqueId);

    Optional<Company> findById(Long companyId);

   //  CompanyDetailDTO getCompanyByUniqueId(String companyUniqueId);
  
    User addEmployerToCompany(EmployeeRegistrationDTO dto);

    User findCompanyAdminByEmail(String email);

    User findEmployerByEmail(String email);

    List<EmployerProfileDTO> getCompanyEmployees(String companyUniqueId);

    boolean isCompanyAdmin(User user);

    CompanyDetailDTO updateCompanyProfile(CompanyProfileUpdateDTO profileDTO);

     void deleteCompanyById(String companyUniqueId);

     Company updateCompanyBanner(Long companyId, MultipartFile bannerFile) throws IOException;

     Company removeCompanyBanner(Long companyId);

     CompanyWithMediaDto getCompanyWithMedia(Long companyId);
}
