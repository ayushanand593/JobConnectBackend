package com.DcoDe.jobconnect.repositories;

import com.DcoDe.jobconnect.entities.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    Page<JobApplication> findByCandidateId(Long candidateId, Pageable pageable);
    Page<JobApplication> findByJobId(Long jobId, Pageable pageable);
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);
}