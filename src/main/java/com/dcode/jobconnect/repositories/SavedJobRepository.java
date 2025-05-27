package com.dcode.jobconnect.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dcode.jobconnect.entities.Candidate;
import com.dcode.jobconnect.entities.Job;
import com.dcode.jobconnect.entities.SavedJob;


import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    
    List<SavedJob> findByCandidateId(Long candidateId);
    
    List<SavedJob> findByJobJobId(String jobId);
    
    Optional<SavedJob> findByCandidateAndJob(Candidate candidate, Job job);
    
    boolean existsByCandidateAndJob(Candidate candidate, Job job);

    
    @Modifying
    @Query("DELETE FROM SavedJob sj WHERE sj.job.jobId = ?1")
    void deleteByJobId(String jobId);
    
    @Modifying
    @Query("DELETE FROM SavedJob sj WHERE sj.candidate.id = ?1")
    void deleteByCandidateId(Long candidateId);

    @Modifying
    void deleteByJob(Job job);

    @Modifying
    @Query("DELETE FROM SavedJob sj WHERE sj.job = :job")
    int deleteSavedJobsByJob(@Param("job") Job job);


    @Modifying
@Query("DELETE FROM SavedJob sj WHERE sj.job.id IN :jobIds")
void deleteByJobIds(@Param("jobIds") List<Long> jobIds);

@Modifying  
@Query(value = "DELETE FROM saved_jobs WHERE job_id IN (SELECT id FROM jobs WHERE company_id = :companyId)", nativeQuery = true)
void deleteByCompanyId(@Param("companyId") Long companyId);

      @Modifying
    @Query("DELETE FROM SavedJob sj WHERE sj.job = :job")
    int Number_Of_Deleted_Job(Job job);

}
