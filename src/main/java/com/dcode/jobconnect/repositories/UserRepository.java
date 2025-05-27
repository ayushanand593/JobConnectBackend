package com.dcode.jobconnect.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("SELECT u FROM User u JOIN FETCH u.employerProfile ep JOIN u.company c WHERE c.companyUniqueId = :companyUniqueId")
    List<User> findWithProfilesByCompanyCompanyUniqueId(@Param("companyUniqueId") String companyUniqueId);
   
    List<User> findByCompany(Company company);


@Modifying  
@Query("DELETE FROM User u WHERE u.company.id = :companyId AND u.role = 'EMPLOYER'")
void deleteEmployersByCompanyId(@Param("companyId") Long companyId);


@Modifying
@Query("DELETE FROM User u WHERE u.company.id = :companyId")
void deleteAllByCompanyId(@Param("companyId") Long companyId);

@Modifying
@Query(value = "UPDATE users SET company_id = NULL WHERE company_id = :companyId AND role = 'CANDIDATE'", nativeQuery = true)
void clearCompanyFromCandidates(@Param("companyId") Long companyId);

@Modifying  
@Query(value = "UPDATE users SET company_id = NULL WHERE company_id = :companyId AND role = 'ADMIN'", nativeQuery = true)
void clearCompanyFromAdmins(@Param("companyId") Long companyId);
}
