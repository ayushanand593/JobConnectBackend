package com.DcoDe.jobconnect.controller;

import com.DcoDe.jobconnect.entities.FileDocument;
import com.DcoDe.jobconnect.services.interfaces.FileStorageServiceI;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageServiceI fileStorageService;

    @GetMapping("/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileId) {
        FileDocument fileDocument = fileStorageService.getFile(fileId);
        byte[] fileData = fileStorageService.getFileData(fileId);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileDocument.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDocument.getFileName() + "\"")
                .body(fileData);
    }
}