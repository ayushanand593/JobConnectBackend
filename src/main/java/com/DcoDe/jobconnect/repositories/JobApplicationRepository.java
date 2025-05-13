package com.DcoDe.jobconnect.repositories;

import com.DcoDe.jobconnect.dto.JobApplicationDTO;
import com.DcoDe.jobconnect.entities.JobApplication;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    Page<JobApplication> findByCandidateId(Long candidateId, Pageable pageable);
    List<JobApplication> findByCandidateId(Long candidateId);
    Page<JobApplication> findByJobId(Long jobId, Pageable pageable);
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    @Query("SELECT a FROM JobApplication  a WHERE a.job.jobId = :jobId")
    List<JobApplication> findByJobId(@Param("jobId") String jobId);
}