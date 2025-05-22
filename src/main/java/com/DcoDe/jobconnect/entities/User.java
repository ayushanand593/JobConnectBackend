package com.DcoDe.jobconnect.entities;
import com.DcoDe.jobconnect.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonBackReference
    private Company company;  // For company role

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
   @JsonIgnore // Add this to prevent circular reference in JSON serialization
    @ToString.Exclude // For lombok users
    @EqualsAndHashCode.Exclude // This excludes from equals/hashCode calculations
    private Candidate candidateProfile;  // For CANDIDATE role

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonBackReference
    private EmployerProfile employerProfile;  // For EMPLOYER role

    @Column(name = "terms_accepted")
    private Boolean termsAccepted = false;

    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt;

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

     // Fix for hashCode/equals to prevent circular reference
     @Override
     public int hashCode() {
         return Objects.hash(id, email, role);
         // Don't include candidateProfile in hashCode calculation
     }
     
     @Override
     public boolean equals(Object obj) {
         if (this == obj) return true;
         if (obj == null || getClass() != obj.getClass()) return false;
         User user = (User) obj;
         return Objects.equals(id, user.id) && 
                Objects.equals(email, user.email) && 
                role == user.role;
         // Don't include candidateProfile in equals comparison
     }
}



