package com.dcode.jobconnect.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dcode.jobconnect.entities.DisclosureAnswer;
import com.dcode.jobconnect.entities.Job;

import java.util.List;

@Repository
public interface DisclosureAnswerRepository extends JpaRepository<DisclosureAnswer, Long> {
    List<DisclosureAnswer> findAllByJobApplicationId(Long applicationId);
     
    @Modifying
    @Query("DELETE FROM DisclosureAnswer da WHERE da.jobApplication.job.id = :jobId")
    int deleteByJobId(@Param("jobId") Long jobId);
    
    @Modifying
    @Query("DELETE FROM DisclosureAnswer da WHERE da.jobApplication.job = :job")
    int deleteByJob(@Param("job") Job job);
    
    @Modifying
    @Query("DELETE FROM DisclosureAnswer da WHERE da.jobApplication.id = :applicationId")
    int deleteByJobApplicationId(@Param("applicationId") Long applicationId);
}
