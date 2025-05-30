package com.dcode.jobconnect.services.interfaces;

import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.dcode.jobconnect.entities.FileDocument;
import com.dcode.jobconnect.services.FileStorageServiceImpl.LogoInfo;

public interface FileStorageServiceI {
    /**
     * Upload a file and return a reference ID
     * @param file File to upload
     * @return File ID reference
     */
    String uploadFile(MultipartFile file);

     Optional<FileDocument> getCompanyLogo(Long companyId);

     String getLogoAsBase64(Long companyId);

     LogoInfo getCompanyLogoInfo(Long companyId);
    
    /**
     * Delete a file by its reference ID
     * @param fileId File ID to delete
     */
    void deleteFile(String fileId);
    
    /**
     * Get a file by its reference ID
     * @param fileId File ID
     * @return The FileDocument entity
     */
    FileDocument getFile(String fileId);
    
    /**
     * Get file data (decompressed if needed)
     * @param fileId File ID
     * @return Byte array of file data
     */
    byte[] getFileData(String fileId);
}
