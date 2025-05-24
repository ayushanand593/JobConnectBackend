package com.dcode.jobconnect.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dcode.jobconnect.entities.Company;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Long> {
    Optional<Company> findByCompanyUniqueId(String companyUniqueId);
    
    
    Page<Company> findByCompanyNameContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("SELECT DISTINCT c FROM Company c LEFT JOIN FETCH c.admins WHERE c.id = :companyId")
    Optional<Company> findWithAdminsById(Long companyId);
}
