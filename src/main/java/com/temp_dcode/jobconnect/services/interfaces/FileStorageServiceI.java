package com.dcode.jobconnect.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

import com.dcode.jobconnect.entities.FileDocument;

public interface FileStorageServiceI {
    /**
     * Upload a file and return a reference ID
     * @param file File to upload
     * @return File ID reference
     */
    String uploadFile(MultipartFile file);
    
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
