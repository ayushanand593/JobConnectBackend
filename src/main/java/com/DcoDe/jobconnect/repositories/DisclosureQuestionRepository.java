package com.DcoDe.jobconnect.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.DcoDe.jobconnect.entities.DisclosureQuestion;

@Repository
public interface DisclosureQuestionRepository extends JpaRepository<DisclosureQuestion, Long> {
    List<DisclosureQuestion> findAllByJobId(Long jobId);
}
