package com.dcode.jobconnect.services;

import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dcode.jobconnect.entities.Company;
import com.dcode.jobconnect.exceptions.CompanyNotFoundException;
import com.dcode.jobconnect.exceptions.InvalidImageException;
import com.dcode.jobconnect.repositories.CompanyRepository;
import com.dcode.jobconnect.services.interfaces.CompanyImageServiceI;
import com.dcode.jobconnect.services.interfaces.FileStorageServiceI;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Image;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CompanyImageServiceImpl implements CompanyImageServiceI {

    private final CompanyRepository companyRepository;
    private final FileStorageServiceI fileStorageService;
    
    // Allowed content types for images
    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg","image/jpg", "image/png", "image/gif", "image/svg+xml"
    ));
    
    // Image size limits
    private static final int MAX_LOGO_SIZE_KB = 500; // 500KB
    private static final int MAX_BANNER_SIZE_KB = 2048; // 2MB
    
    // Image dimension limits
    private static final int LOGO_MAX_WIDTH = 400;
    private static final int LOGO_MAX_HEIGHT = 400;
    private static final int BANNER_MAX_WIDTH = 1200;
    private static final int BANNER_MAX_HEIGHT = 300;

    @Override
    @Transactional
    public String uploadCompanyLogo(String companyUniqueId, MultipartFile file) {
        validateImageFile(file, MAX_LOGO_SIZE_KB);
        
        Company company = companyRepository.findByCompanyUniqueId(companyUniqueId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyUniqueId));
        
        // If company already has a logo, delete the old one
        if (company.getLogoFileId() != null) {
            fileStorageService.deleteFile(company.getLogoFileId());
        }
        
        try {
            // Resize logo image before storing
            byte[] resizedImageData = resizeImage(file.getBytes(), LOGO_MAX_WIDTH, LOGO_MAX_HEIGHT, file.getContentType());
            String fileId = uploadResizedFile(file, resizedImageData);
            
            company.setLogoFileId(fileId);
            companyRepository.save(company);
            
            return fileId;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to process logo image", e);
        }
    }

    @Override
    @Transactional
    public String uploadCompanyBanner(String companyUniqueId, MultipartFile file) {
        validateImageFile(file, MAX_BANNER_SIZE_KB);
        
        Company company = companyRepository.findByCompanyUniqueId(companyUniqueId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyUniqueId));
        
        // If company already has a banner, delete the old one
        if (company.getBannerFileId() != null) {
            fileStorageService.deleteFile(company.getBannerFileId());
        }
        
        try {
            // Resize banner image before storing
            byte[] resizedImageData = resizeImage(file.getBytes(), BANNER_MAX_WIDTH, BANNER_MAX_HEIGHT, file.getContentType());
            String fileId = uploadResizedFile(file, resizedImageData);
            
            company.setBannerFileId(fileId);
            companyRepository.save(company);
            
            return fileId;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to process banner image", e);
        }
    }
    
    private String uploadResizedFile(MultipartFile originalFile, byte[] resizedData){
        // Create a wrapper MultipartFile with the resized data
        MultipartFile resizedFile = new MultipartFile() {
            @Override
            public @NonNull String getName() {
                return originalFile.getName();
            }

            @Override
            public String getOriginalFilename() {
                return originalFile.getOriginalFilename();
            }

            @Override
            public String getContentType() {
                return originalFile.getContentType();
            }

            @Override
            public boolean isEmpty() {
                return resizedData.length == 0;
            }

            @Override
            public long getSize() {
                return resizedData.length;
            }

            @Override
            public @NonNull byte[] getBytes() {
                return resizedData;
            }

            @Override
            public @NonNull java.io.InputStream getInputStream() {
                return new ByteArrayInputStream(resizedData);
            }

            @Override
            public  void transferTo( @NonNull java.io.File dest) throws IOException, IllegalStateException {
                throw new UnsupportedOperationException("Transfer operation not supported");
            }
        };
        
        return fileStorageService.uploadFile(resizedFile);
    }
    
    private void validateImageFile(MultipartFile file, int maxSizeKB) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new InvalidImageException("File is empty");
        }
        
        // Check file content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidImageException("Invalid file type. Allowed types: JPEG, JPG, PNG, GIF, SVG");
        }
        
        // Check file size
        if (file.getSize() > maxSizeKB * 1024) { // Convert KB to bytes
            throw new InvalidImageException("File size exceeds maximum limit of " + maxSizeKB + "KB");
        }
    }
    
    private byte[] resizeImage(byte[] imageData, int maxWidth, int maxHeight, String contentType) {
        ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
        BufferedImage originalImage;
        try {
            originalImage = ImageIO.read(bis);
                
        // If image is null (could happen with some SVG files), return original data
        if (originalImage == null) {
            return imageData;
        }
        
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // Check if resizing is needed
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return imageData; // No need to resize
        }
        
        // Calculate new dimensions while maintaining aspect ratio
        float aspectRatio = (float) originalWidth / originalHeight;
        int newWidth;
        int newHeight;
        
        if (originalWidth > originalHeight) {
            newWidth = maxWidth;
            newHeight = Math.round(newWidth / aspectRatio);
            if (newHeight > maxHeight) {
                newHeight = maxHeight;
                newWidth = Math.round(newHeight * aspectRatio);
            }
        } else {
            newHeight = maxHeight;
            newWidth = Math.round(newHeight * aspectRatio);
            if (newWidth > maxWidth) {
                newWidth = maxWidth;
                newHeight = Math.round(newWidth / aspectRatio);
            }
        }
        
        // Create resized image
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();
        
        // Convert to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        // Use the same format as original
        String formatName = "jpg";
        if (contentType != null && contentType.contains("/")) {
            formatName = contentType.split("/")[1];
            if (formatName.equals("jpeg")) {
                formatName = "jpg";
            } else if (formatName.equals("svg+xml")) {
                // SVG can't be resized this way, return original
                return imageData;
            }
        }
        
        ImageIO.write(resizedImage, formatName, bos);
        return bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to process image: " , e);
        }
    
    }
}