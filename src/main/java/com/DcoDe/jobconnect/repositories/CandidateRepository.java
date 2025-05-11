package com.DcoDe.jobconnect.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.DcoDe.jobconnect.entities.Candidate;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
        Optional<Candidate> findByUserId(Long userId);
}
