package com.dcode.jobconnect.entities;

import java.time.LocalDateTime;

import com.dcode.jobconnect.enums.FileType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "company_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "company_id", nullable = false)
    private Long companyId;
    
    @Column(name = "file_id", nullable = false)
    private String fileId;
    
    @Column(name = "file_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private FileType fileType;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
