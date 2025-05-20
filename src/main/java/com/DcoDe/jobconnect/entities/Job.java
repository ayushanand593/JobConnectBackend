package com.DcoDe.jobconnect.entities;


import com.DcoDe.jobconnect.enums.JobStatus;
import com.DcoDe.jobconnect.enums.JobType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

     @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    @JsonBackReference("company-jobs")  // Add this
    private Company company;

    @ManyToOne
    @JoinColumn(name = "posted_by", nullable = false)
    @JsonBackReference("user-jobs")  // Add this
    private User postedBy;

    @Column(nullable = false)
    private String title;

    @Column(name = "job_id", nullable = false)
    private String jobId;

    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    private JobType jobType;

    @Column(name = "experience_required")
    private String experienceLevel;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String description;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String responsibilities;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String requirements;

    @Column(name = "salary_range")
    private String salaryRange;

    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.OPEN;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

      @ManyToMany
    @JoinTable(
            name = "job_skills",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @JsonIgnore  // Change to @JsonIgnore instead of @ToString.Exclude
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Skill> skills = new HashSet<>();

 @OneToMany(mappedBy = "job")
    @JsonIgnore  // Change to @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<JobApplication> applications = new ArrayList<>();

        @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore  // Add this to prevent recursion
    private List<DisclosureQuestion> disclosureQuestions = new ArrayList<>();

   @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("job-savedjobs")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<SavedJob> savedJobs = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

