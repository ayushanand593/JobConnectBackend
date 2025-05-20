package com.DcoDe.jobconnect.entities;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_unique_id", nullable = false, unique = true)
    private String companyUniqueId;

    private String industry;
    private String size;
    private String website;
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;
     @Column(name = "logo_file_id")
    private String logoFileId;
    
    // Added field for company banner
    @Column(name = "banner_file_id")
    private String bannerFileId;



//    @EqualsAndHashCode.Exclude
      @OneToMany(mappedBy = "company")
    @JsonIgnore  // Add this to prevent recursion
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<User> employerUsers = new ArrayList<>();


//    @EqualsAndHashCode.Exclude
@OneToMany(mappedBy = "company")
    @JsonIgnore  // Add this to prevent recursion
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Job> jobs= new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "company_admins",
            joinColumns = @JoinColumn(name = "company_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> admins = new HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
