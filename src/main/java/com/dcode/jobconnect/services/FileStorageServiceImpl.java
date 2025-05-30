package com.dcode.jobconnect.services;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dcode.jobconnect.entities.CompanyFile;
import com.dcode.jobconnect.entities.FileDocument;
import com.dcode.jobconnect.enums.FileType;
import com.dcode.jobconnect.repositories.CompanyFileRepository;
import com.dcode.jobconnect.repositories.FileDocumentRepository;
import com.dcode.jobconnect.services.interfaces.FileStorageServiceI;

import jakarta.transaction.Transactional;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements FileStorageServiceI {

    private final FileDocumentRepository fileDocumentRepository;
    private final CompanyFileRepository companyFileRepository;

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            // Generate unique ID for the file
            String fileId = UUID.randomUUID().toString();
            
            FileDocument fileDocument = new FileDocument();
            fileDocument.setFileName(file.getOriginalFilename());
            fileDocument.setFileId(fileId);
            fileDocument.setContentType(file.getContentType());
            fileDocument.setSize(file.getSize());
            
            // Compress and store the file content
            fileDocument.setData(compressData(file.getBytes()));
            fileDocument.setCompressed(true);
            
            // Save to database
            fileDocumentRepository.save(fileDocument);
            
            // Return a reference to the file that can be used later
            return fileId;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not store file " + file.getOriginalFilename(), ex);
        }
    }
    
    @Override
    public void deleteFile(String fileId) {
        fileDocumentRepository.deleteByFileId(fileId);
    }
    
    public FileDocument getFile(String fileId) {
        return fileDocumentRepository.findByFileId(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));
    }
    
    public byte[] getFileData(String fileId) {
        FileDocument fileDocument = getFile(fileId);
        
        // Decompress if needed
        if (fileDocument.isCompressed()) {
            return decompressData(fileDocument.getData());
        }
        
        return fileDocument.getData();
    }

     public Optional<FileDocument> getCompanyLogo(Long companyId) {
        try {
            Optional<CompanyFile> companyFileOpt = companyFileRepository.findLogoByCompanyId(companyId);
            if (companyFileOpt.isPresent()) {
                String fileId = companyFileOpt.get().getFileId();
                return fileDocumentRepository.findByFileId(fileId);
            }
        } catch (Exception e) {
            log.warn("Failed to find logo for company {} using mapping table: {}", companyId, e.getMessage());
        }
        return Optional.empty();
    }

    //  public Optional<FileDocument> getCompanyLogo(Long companyId) {
    //     return fileDocumentRepository.findLogoByCompanyId(companyId);
    // }
    
    // Method to get logo data as base64
    public String getLogoAsBase64(Long companyId) {
        Optional<FileDocument> logoDoc = getCompanyLogo(companyId);
        if (logoDoc.isPresent()) {
            byte[] logoData = getFileData(logoDoc.get().getFileId());
            return Base64.getEncoder().encodeToString(logoData);
        }
        return null;
    }
    
    // Method to get complete logo info
    // public LogoInfo getCompanyLogoInfo(Long companyId) {
    //     Optional<FileDocument> logoDoc = getCompanyLogo(companyId);
    //     if (logoDoc.isPresent()) {
    //         FileDocument doc = logoDoc.get();
    //         byte[] logoData = getFileData(doc.getFileId());
    //         String base64Data = Base64.getEncoder().encodeToString(logoData);
            
    //         return LogoInfo.builder()
    //                 .fileId(doc.getFileId())
    //                 .base64Data(base64Data)
    //                 .contentType(doc.getContentType())
    //                 .fileName(doc.getFileName())
    //                 .build();
    //     }
    //     return null;
    // }
    
    // Helper class for logo information
    // @Builder
    // @Data
    // public static class LogoInfo {
    //     private String fileId;
    //     private String base64Data;
    //     private String contentType;
    //     private String fileName;
        
    //     public String getDataUrl() {
    //         return "data:" + contentType + ";base64," + base64Data;
    //     }
    // }
    
   private byte[] compressData(byte[] data) {
    try {
        // Check if data is already GZIP compressed by checking GZIP magic number
        if (isAlreadyCompressed(data)) {
            return data; // Already compressed, return as-is
        }
        return compress(data); // Not compressed, compress it
    } catch (Exception e) {
        throw new IllegalArgumentException("Error compressing data", e);
    }
}
    
    private byte[] decompressData(byte[] data) {
        try {
            return decompress(data);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error decompressing data", e);
        }
    }
    
    // Compress the byte array
    private byte[] compress(byte[] data) throws IOException {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream(data.length);
        java.util.zip.GZIPOutputStream gzip = new java.util.zip.GZIPOutputStream(bos);
        gzip.write(data);
        gzip.close();
        byte[] compressed = bos.toByteArray();
        bos.close();
        return compressed;
    }
    
    // Decompress the byte array
    private byte[] decompress(byte[] compressed) throws IOException {
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(compressed);
        java.util.zip.GZIPInputStream gis = new java.util.zip.GZIPInputStream(bis);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        
        byte[] buffer = new byte[1024];
        int len;
        while ((len = gis.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        
        byte[] decompressed = bos.toByteArray();
        bos.close();
        gis.close();
        bis.close();
        return decompressed;
    }
    private boolean isAlreadyCompressed(byte[] data) {
    // GZIP files start with magic number: 0x1f 0x8b
    return data != null && 
           data.length >= 2 && 
           (data[0] & 0xFF) == 0x1f && 
           (data[1] & 0xFF) == 0x8b;
}
   public LogoInfo getCompanyLogoInfo(Long companyId) {
        Optional<FileDocument> logoDoc = getCompanyLogo(companyId);
        if (logoDoc.isEmpty()) {
            logoDoc = getCompanyLogoByPattern(companyId); // Fallback
        }
        
        if (logoDoc.isPresent()) {
            FileDocument doc = logoDoc.get();
            try {
                byte[] logoData = doc.getData();
                String base64Data = Base64.getEncoder().encodeToString(logoData);
                
                return LogoInfo.builder()
                        .fileId(doc.getFileId())
                        .base64Data(base64Data)
                        .contentType(doc.getContentType())
                        .fileName(doc.getFileName())
                        .build();
            } catch (Exception e) {
                log.error("Error processing logo data for company {}: {}", companyId, e.getMessage());
            }
        }
        return null;
    }
    
    // Method to save company logo with mapping
    @Transactional
    public String saveCompanyLogo(Long companyId, MultipartFile file) throws IOException {
          // Check for an existing logo mapping
    Optional<CompanyFile> existingMapping = companyFileRepository.findLogoByCompanyId(companyId);
    if (existingMapping.isPresent()) {
        String oldFileId = existingMapping.get().getFileId();
        // Delete old file record
        fileDocumentRepository.deleteByFileId(oldFileId);
        // Delete mapping record
        companyFileRepository.delete(existingMapping.get());
    }

    // Save the new file first
    String fileId = UUID.randomUUID().toString();
    
    FileDocument fileDocument = new FileDocument();
    fileDocument.setFileId(fileId);
    fileDocument.setFileName(file.getOriginalFilename());
    fileDocument.setContentType(file.getContentType());
    fileDocument.setSize(file.getSize());
    // Optionally compress if needed
    fileDocument.setData(file.getBytes());
    fileDocument.setCompressed(false);
    
    fileDocumentRepository.save(fileDocument);
    
    // Create a new mapping
    CompanyFile companyFile = new CompanyFile();
    companyFile.setCompanyId(companyId);
    companyFile.setFileId(fileId);
    companyFile.setFileType(FileType.LOGO);
    
    companyFileRepository.save(companyFile);
    
    return fileId;
    }
  
        // Get banner by file ID (direct from company.bannerFileId)
    public Optional<FileDocument> getBannerByFileId(String fileId) {
        if (fileId == null || fileId.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return fileDocumentRepository.findByFileId(fileId);
        } catch (Exception e) {
            log.warn("Failed to find banner with fileId {}: {}", fileId, e.getMessage());
            return Optional.empty();
        }
    }
    
    // Get banner info with base64 encoding
    public BannerInfo getBannerInfo(String fileId) {
        Optional<FileDocument> bannerDoc = getBannerByFileId(fileId);
        
        if (bannerDoc.isPresent()) {
            FileDocument doc = bannerDoc.get();
            try {
                byte[] bannerData = doc.getData();
                String base64Data = Base64.getEncoder().encodeToString(bannerData);
                
                return BannerInfo.builder()
                        .fileId(doc.getFileId())
                        .base64Data(base64Data)
                        .contentType(doc.getContentType())
                        .fileName(doc.getFileName())
                        .build();
            } catch (Exception e) {
                log.error("Error processing banner data for fileId {}: {}", fileId, e.getMessage());
            }
        }
        return null;
    }

      @Transactional
    public String saveBannerFile(MultipartFile file) throws IOException {
        String fileId = UUID.randomUUID().toString();
        
        FileDocument fileDocument = new FileDocument();
        fileDocument.setFileId(fileId);
        fileDocument.setFileName(file.getOriginalFilename());
        fileDocument.setContentType(file.getContentType());
        fileDocument.setSize(file.getSize());
        fileDocument.setData(file.getBytes());
        fileDocument.setCompressed(false);
        
        fileDocumentRepository.save(fileDocument);
        
        log.info("Banner file saved with fileId: {}", fileId);
        return fileId;
    }
    
    // Delete banner file
    @Transactional
    public void deleteBannerFile(String fileId) {
        if (fileId != null && !fileId.trim().isEmpty()) {
            try {
                fileDocumentRepository.deleteByFileId(fileId);
                log.info("Banner file deleted with fileId: {}", fileId);
            } catch (Exception e) {
                log.error("Error deleting banner file with fileId {}: {}", fileId, e.getMessage());
            }
        }
    }
    
    @Builder
    @Data
    public static class LogoInfo {
        private String fileId;
        private String base64Data;
        private String contentType;
        private String fileName;
        
        public String getDataUrl() {
            if (base64Data != null && contentType != null) {
                return "data:" + contentType + ";base64," + base64Data;
            }
            return null;
        }
    }

    @Builder
    @Data
    public static class BannerInfo {
        private String fileId;
        private String base64Data;
        private String contentType;
        private String fileName;
        
        public String getDataUrl() {
            if (base64Data != null && contentType != null) {
                return "data:" + contentType + ";base64," + base64Data;
            }
            return null;
        }
    }
      public Optional<FileDocument> getCompanyLogoByPattern(Long companyId) {
        try {
            // Try different patterns
            Optional<FileDocument> logo = fileDocumentRepository.findLogoByCompanyIdFromFileName(companyId);
            if (logo.isEmpty()) {
                logo = fileDocumentRepository.findLogoByCompanyIdPattern(companyId);
            }
            return logo;
        } catch (Exception e) {
            log.warn("Failed to find logo for company {} using filename pattern: {}", companyId, e.getMessage());
            return Optional.empty();
        }
    }
}
