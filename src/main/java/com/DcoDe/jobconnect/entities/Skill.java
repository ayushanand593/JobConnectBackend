package com.DcoDe.jobconnect.entities;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

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
      @ToString.Exclude  // Prevent circular toString() calls
    @EqualsAndHashCode.Exclude  // Prevent circular equals/hashCode calls
    private Set<Job> jobs = new HashSet<>();

    @ManyToMany(mappedBy = "skills")
    private Set<Candidate> candidates = new HashSet<>();
}
