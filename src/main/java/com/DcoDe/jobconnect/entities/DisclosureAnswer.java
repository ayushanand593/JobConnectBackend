package com.DcoDe.jobconnect.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "disclosure_answers")
@Data
@NoArgsConstructor
public class DisclosureAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "application_id", nullable = false)
    private JobApplication jobApplication;
    
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private DisclosureQuestion question;
    
    @Column(name = "answer_text", nullable = false)
    private String answerText;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}