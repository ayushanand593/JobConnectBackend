package com.dcode.jobconnect.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dcode.jobconnect.entities.EmployerProfile;

import java.util.Optional;

@Repository
public interface EmployerProfileRepository extends JpaRepository<EmployerProfile, Long> {
    Optional<EmployerProfile> findByUserId(Long userId);

@Modifying
@Query(value = "DELETE FROM employer_profiles WHERE user_id IN (SELECT id FROM users WHERE company_id = :companyId AND role = 'EMPLOYER')", nativeQuery = true)
void deleteByCompanyId(@Param("companyId") Long companyId);
}

