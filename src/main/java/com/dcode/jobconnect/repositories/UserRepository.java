package com.dcode.jobconnect.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dcode.jobconnect.entities.Company;
import com.dcode.jobconnect.entities.User;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.company c WHERE c.companyUniqueId = :companyUniqueId")
    List<User> findByCompanyCompanyUniqueId(@Param("companyUniqueId") String companyUniqueId);
    
    // New method to get users with their employer profiles
    @Query("SELECT u FROM User u JOIN FETCH u.employerProfile ep JOIN u.company c WHERE c.companyUniqueId = :companyUniqueId")
    List<User> findWithProfilesByCompanyCompanyUniqueId(@Param("companyUniqueId") String companyUniqueId);
   
    List<User> findByCompany(Company company);

}
