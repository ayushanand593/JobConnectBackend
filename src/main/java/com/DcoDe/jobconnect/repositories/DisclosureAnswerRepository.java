package com.DcoDe.jobconnect.repositories;


import com.DcoDe.jobconnect.entities.DisclosureAnswer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisclosureAnswerRepository extends JpaRepository<DisclosureAnswer, Long> {
    List<DisclosureAnswer> findAllByJobApplicationId(Long applicationId);
}
