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
    void deleteByJob(Job job);

    @Modifying
    @Query("DELETE FROM DisclosureQuestion dq WHERE dq.job = :job")
    int deleteByJobEntity(@Param("job") Job job);

    @Modifying
@Query(value = "DELETE FROM disclosure_questions WHERE job_id IN (SELECT id FROM jobs WHERE company_id = :companyId)", nativeQuery = true) 
void deleteByCompanyId(@Param("companyId") Long companyId);

    @Modifying
@Query("DELETE FROM DisclosureQuestion dq WHERE dq.job.id IN :jobIds")
void deleteByJobIds(@Param("jobIds") List<Long> jobIds);

      @Modifying
    @Query("DELETE FROM DisclosureQuestion dq WHERE dq.job = :job")
    int Number_Of_Deleted_Job(Job job);
}
