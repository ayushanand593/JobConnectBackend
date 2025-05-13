package com.DcoDe.jobconnect.entities;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "candidates")
@Data
@NoArgsConstructor
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // Add this to prevent circular reference in JSON serialization
    @ToString.Exclude // For lombok users
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String phone;
    private String headline;
    private String summary;

    @Column(name = "experience_years")
    private Integer experienceYears;

    // @Column(name = "resume_url")
    // private String resumeUrl;
      @Column(name = "resume_file_id")
    private String resumeFileId;

    @ManyToMany
    @JoinTable(
            name = "candidate_skills",
            joinColumns = @JoinColumn(name = "candidate_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    // @OneToMany(mappedBy = "candidate")
    // private List<JobApplication> applications = new ArrayList<>();

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, phone, headline, summary, experienceYears, resumeFileId);
        // Don't include user in hashCode calculation
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Candidate candidate = (Candidate) obj;
        return Objects.equals(id, candidate.id) && 
               Objects.equals(firstName, candidate.firstName) && 
               Objects.equals(lastName, candidate.lastName);
        // Don't include user in equals comparison
    }
}

