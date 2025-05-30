package com.dcode.jobconnect.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dcode.jobconnect.entities.Company;
import com.dcode.jobconnect.entities.CompanyFile;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Long> {
    Optional<Company> findByCompanyUniqueId(String companyUniqueId);


    
@Modifying
@Query(value = "DELETE FROM company_admins WHERE company_id = :companyId", nativeQuery = true)
void clearCompanyAdmins(@Param("companyId") Long companyId);

@Query("SELECT COUNT(ca) > 0 FROM Company c JOIN c.admins ca WHERE ca.id = :userId AND c.id = :companyId")
boolean isUserAdminOfCompany(@Param("userId") Long userId, @Param("companyId") Long companyId);

@Query("SELECT cf FROM CompanyFile cf WHERE cf.companyId = :companyId AND cf.fileType = 'LOGO'")
    Optional<CompanyFile> findLogoByCompanyId(@Param("companyId") Long companyId);

}
