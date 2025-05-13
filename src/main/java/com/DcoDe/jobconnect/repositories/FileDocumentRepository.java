package com.DcoDe.jobconnect.repositories;

import com.DcoDe.jobconnect.entities.FileDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileDocumentRepository extends JpaRepository<FileDocument, Long> {
    Optional<FileDocument> findByFileId(String fileId);
    void deleteByFileId(String fileId);
}