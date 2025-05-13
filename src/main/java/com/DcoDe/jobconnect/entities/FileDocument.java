package com.DcoDe.jobconnect.entities;

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
    
    @Column
    private boolean compressed = false;
}
