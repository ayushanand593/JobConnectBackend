package com.dcode.jobconnect.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dcode.jobconnect.entities.Candidate;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

        Optional<Candidate> findByUserId(Long userId);

        @Query("SELECT c FROM Candidate c JOIN FETCH c.skills WHERE c.id = :id")
        Optional<Candidate> findByIdWithSkills(Long id);

        @Modifying
@Query(value = "DELETE FROM candidate_skills WHERE candidate_id = :candidateId", nativeQuery = true)
void deleteCandidateSkillsByCandidateId(@Param("candidateId") Long candidateId);

  boolean existsByResumeFileId(String resumeFileId);
}
