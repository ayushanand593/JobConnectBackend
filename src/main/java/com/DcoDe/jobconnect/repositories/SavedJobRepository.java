package com.DcoDe.jobconnect.repositories;

import com.DcoDe.jobconnect.entities.Candidate;
import com.DcoDe.jobconnect.entities.Candidate;
import com.DcoDe.jobconnect.entities.Job;
import com.DcoDe.jobconnect.entities.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    
    List<SavedJob> findByCandidateId(Long candidateId);
    
    List<SavedJob> findByJobJobId(String jobId);
    
    Optional<SavedJob> findByCandidateAndJob(Candidate candidate, Job job);
    
    boolean existsByCandidateAndJob(Candidate candidate, Job job);
    
    @Modifying
    @Query("DELETE FROM SavedJob sj WHERE sj.candidate.id = ?1 AND sj.job.jobId = ?2")
    void deleteByCandidateIdAndJobId(Long candidateId, String jobId);
    
    @Modifying
    @Query("DELETE FROM SavedJob sj WHERE sj.job.jobId = ?1")
    void deleteByJobId(String jobId);
    
    @Modifying
    @Query("DELETE FROM SavedJob sj WHERE sj.candidate.id = ?1")
    void deleteByCandidateId(Long candidateId);
}
