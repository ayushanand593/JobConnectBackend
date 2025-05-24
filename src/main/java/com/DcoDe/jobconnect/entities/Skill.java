package com.dcode.jobconnect.entities;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "skills")
@Data
@NoArgsConstructor
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

     @ManyToMany(mappedBy = "skills")
    @JsonIgnore  // Add this
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Job> jobs = new HashSet<>();

      @ManyToMany(mappedBy = "skills")
    @JsonIgnore  // Add this
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Candidate> candidates = new HashSet<>();
}
