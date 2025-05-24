package com.dcode.jobconnect.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dcode.jobconnect.entities.FileDocument;
import com.dcode.jobconnect.repositories.FileDocumentRepository;
import com.dcode.jobconnect.services.interfaces.FileStorageServiceI;

import java.io.IOException;
// import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageServiceI {

    private final FileDocumentRepository fileDocumentRepository;

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
            throw new RuntimeException("Could not store file " + file.getOriginalFilename(), ex);
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
    
    private byte[] compressData(byte[] data) {
        try {
            return java.util.zip.GZIPOutputStream.class.isInstance(data) ? data : compress(data);
        } catch (Exception e) {
            throw new RuntimeException("Error compressing data", e);
        }
    }
    
    private byte[] decompressData(byte[] data) {
        try {
            return decompress(data);
        } catch (Exception e) {
            throw new RuntimeException("Error decompressing data", e);
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
}
