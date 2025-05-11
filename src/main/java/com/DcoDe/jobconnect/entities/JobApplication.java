package com.DcoDe.jobconnect.entities;


import com.DcoDe.jobconnect.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications")
@Data
@NoArgsConstructor
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;
    
    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;
    
    // This can either reference the candidate's stored resume or a separately uploaded one
    @Column(name = "resume_file_id")
    private String resumeFileId;
    
    // Cover letter is stored as a file
    @Column(name = "cover_letter_file_id")
    private String coverLetterFileId;
    
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;
    
    @Column(name = "voluntary_disclosures")
    private String voluntaryDisclosures;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}