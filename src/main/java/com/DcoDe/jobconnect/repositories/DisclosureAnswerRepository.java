package com.dcode.jobconnect.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dcode.jobconnect.entities.DisclosureAnswer;

import java.util.List;

@Repository
public interface DisclosureAnswerRepository extends JpaRepository<DisclosureAnswer, Long> {
    List<DisclosureAnswer> findAllByJobApplicationId(Long applicationId);
}
