package com.dcode.jobconnect.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dcode.jobconnect.entities.DisclosureQuestion;
import com.dcode.jobconnect.entities.Job;

@Repository
public interface DisclosureQuestionRepository extends JpaRepository<DisclosureQuestion, Long> {
    List<DisclosureQuestion> findAllByJobId(Long jobId);

     void deleteByJob(Job job);

      @Modifying
    @Query("DELETE FROM DisclosureQuestion dq WHERE dq.job = :job")
    int NumberOfDeletedJob(Job job);
}
