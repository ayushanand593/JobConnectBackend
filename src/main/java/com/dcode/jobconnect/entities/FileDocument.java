package com.dcode.jobconnect.entities;

import java.time.LocalDateTime;

import org.springframework.cglib.core.Local;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "file_documents")
@Data
@NoArgsConstructor
public class FileDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String fileId;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String contentType;
    
    private long size;
    
    @Lob
    @Column(length = 16777215) // MEDIUMBLOB - up to 16MB
    private byte[] data;

    @Column(name = "is_snapshot")
    private Boolean isSnapshot = false; // Indicates if this is a copy/snapshot
    
    @Column(name = "original_file_id")
    private String originalFileId; 
    
    @Column
    private boolean compressed = false;

    private LocalDateTime uploadedAt;
}
