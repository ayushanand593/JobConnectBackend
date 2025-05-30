package com.dcode.jobconnect.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dcode.jobconnect.entities.CompanyFile;

@Repository
public interface CompanyFileRepository extends JpaRepository<CompanyFile, Long> {

      @Query("SELECT cf FROM CompanyFile cf WHERE cf.companyId = :companyId AND cf.fileType = 'LOGO'")
    Optional<CompanyFile> findLogoByCompanyId(@Param("companyId") Long companyId);
    
    List<CompanyFile> findByCompanyId(Long companyId);
    
    void deleteByFileId(String fileId);

    @Query("SELECT cf FROM CompanyFile cf WHERE cf.companyId = :companyId AND cf.fileType = 'BANNER'")
    Optional<CompanyFile> findBannerByCompanyId(@Param("companyId") Long companyId);
}