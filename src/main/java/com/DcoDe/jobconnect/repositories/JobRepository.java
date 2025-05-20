package com.DcoDe.jobconnect.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.DcoDe.jobconnect.entities.Job;
import com.DcoDe.jobconnect.enums.JobStatus;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Optional<Job> findByJobId(String jobId);

    List<Job> findAllByPostedById(Long postedById);

      boolean existsByJobId(String jobId);
    
    List<Job> findByCompanyId(Long companyId);
    
    List<Job> findByStatus(JobStatus status);
    
    @Query("SELECT j FROM Job j WHERE j.company.id = ?1 AND j.status = ?2")
    List<Job> findByCompanyIdAndStatus(Long companyId, JobStatus status);
    
    @Query("SELECT j FROM Job j WHERE j.postedBy.id = ?1")
    List<Job> findByPostedBy(Long userId);
}
