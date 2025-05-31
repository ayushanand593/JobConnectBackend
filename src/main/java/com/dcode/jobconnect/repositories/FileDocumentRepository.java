package com.dcode.jobconnect.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dcode.jobconnect.entities.CompanyFile;
import com.dcode.jobconnect.entities.FileDocument;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileDocumentRepository extends JpaRepository<FileDocument, Long> {
    Optional<FileDocument> findByFileId(String fileId);
    void deleteByFileId(String fileId);

    List<FileDocument> findByIsSnapshotTrue();
    boolean existsByFileId(String fileId);
    
@Query("SELECT f FROM FileDocument f WHERE f.fileName LIKE CONCAT('%company_', :companyId, '_logo%')")
    Optional<FileDocument> findLogoByCompanyIdFromFileName(@Param("companyId") Long companyId);
    
    // Alternative: Find by filename containing both company info and logo
    @Query("SELECT f FROM FileDocument f WHERE f.fileName LIKE '%logo%' AND f.fileName LIKE CONCAT('%', :companyId, '%')")
    Optional<FileDocument> findLogoByCompanyIdPattern(@Param("companyId") Long companyId);
    
    // Find all image files that could be logos
    @Query("SELECT f FROM FileDocument f WHERE f.contentType LIKE 'image/%' AND (f.fileName LIKE '%logo%' OR f.fileName LIKE '%brand%')")
    List<FileDocument> findAllPossibleLogos();

     @Query("SELECT cf FROM CompanyFile cf WHERE cf.companyId = :companyId AND cf.fileType = 'LOGO'")
    Optional<CompanyFile> findLogoByCompanyId(@Param("companyId") Long companyId);

      List<FileDocument> findByIsSnapshotTrueAndUploadedAtBefore(LocalDateTime dateTime);


}