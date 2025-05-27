package com.dcode.jobconnect.repositories;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dcode.jobconnect.entities.Candidate;
import com.dcode.jobconnect.entities.Job;
import com.dcode.jobconnect.entities.JobApplication;
import com.dcode.jobconnect.enums.ApplicationStatus;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    Page<JobApplication> findByCandidateId(Long candidateId, Pageable pageable);
    List<JobApplication> findByCandidateId(Long candidateId);
    Page<JobApplication> findByJobId(Long jobId, Pageable pageable);
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    @Query("SELECT a FROM JobApplication  a WHERE a.job.jobId = :jobId")
    List<JobApplication> findByJobId(@Param("jobId") String jobId);

     // Find job applications for a list of jobs and within a date range
    List<JobApplication> findByJobInAndCreatedAtBetween(List<Job> jobs, LocalDateTime start, LocalDateTime end);
    
    // Count job applications for a list of jobs and after a certain date
    long countByJobInAndCreatedAtAfter(List<Job> jobs, LocalDateTime date);

    List<JobApplication> findAllByJobIdIn(Collection<Long> jobIds);

Optional<JobApplication> findByJobIdAndCandidateId(Long jobId, Long candidateId);

   // Remove @Transactional from repository methods
    @Modifying
    void deleteByJob(Job job);

    @Modifying
    @Query("DELETE FROM JobApplication ja WHERE ja.status = :status AND ja.updatedAt < :cutoffDate")
    int deleteByStatusAndUpdatedAtBefore(@Param("status") ApplicationStatus status, @Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Modifying
    @Query("DELETE FROM JobApplication ja WHERE ja.job = :job")
    int deleteByJobEntity(@Param("job") Job job);

    
    @Modifying
    @Query("DELETE FROM JobApplication ja WHERE ja.candidate = :candidate")
    int deleteByCandidate(@Param("candidate") Candidate candidate);

    @Query("SELECT ja.id FROM JobApplication ja WHERE ja.candidate.id = :candidateId")
List<Long> findApplicationIdsByCandidateId(@Param("candidateId") Long candidateId);

@Modifying
@Query("DELETE FROM JobApplication ja WHERE ja.candidate.id = :candidateId")
void deleteByCandidateId(@Param("candidateId") Long candidateId);


@Modifying
@Query("DELETE FROM JobApplication ja WHERE ja.job.id IN :jobIds")
void deleteByJobIds(@Param("jobIds") List<Long> jobIds);

@Modifying
@Query(value = "DELETE FROM job_applications WHERE job_id IN (SELECT id FROM jobs WHERE company_id = :companyId)", nativeQuery = true)
void deleteByCompanyId(@Param("companyId") Long companyId);
    
    // Delete applications by job
    @Modifying
    @Query("DELETE FROM JobApplication ja WHERE ja.job = :job")
    int Number_Of_Deleted_Job(Job job);
}