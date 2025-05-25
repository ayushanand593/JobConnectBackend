package com.dcode.jobconnect.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dcode.jobconnect.entities.DisclosureQuestion;
import com.dcode.jobconnect.entities.Job;

@Repository
public interface DisclosureQuestionRepository extends JpaRepository<DisclosureQuestion, Long> {
    List<DisclosureQuestion> findAllByJobId(Long jobId);

     @Modifying
    @Query("DELETE FROM DisclosureAnswer da WHERE da.question.job.id = :jobId")
    int deleteDisclosureAnswersByJobId(@Param("jobId") Long jobId);

    @Modifying
    @Query("DELETE FROM DisclosureQuestion dq WHERE dq.job.id = :jobId")
    int deleteDisclosureQuestionsByJobId(@Param("jobId") Long jobId);
    
    // Remove @Transactional from repository methods
    @Modifying
    void deleteByJob(Job job);

    @Modifying
    @Query("DELETE FROM DisclosureQuestion dq WHERE dq.job = :job")
    int deleteByJobEntity(@Param("job") Job job);

      @Modifying
    @Query("DELETE FROM DisclosureQuestion dq WHERE dq.job = :job")
    int Number_Of_Deleted_Job(Job job);
}
