package com.dcode.jobconnect.entities;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employer_profiles")
@Data
@NoArgsConstructor
public class EmployerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = true)
    private String lastName;

    private String phone;

    @Column(name = "job_title")
    private String jobTitle;
    
    @Column(name = "profile_picture_url") // New column for profile picture
    private String profilePictureUrl;
}
