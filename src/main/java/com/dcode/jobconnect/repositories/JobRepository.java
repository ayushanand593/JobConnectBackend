package com.dcode.jobconnect.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dcode.jobconnect.dto.JobDTO;
import com.dcode.jobconnect.entities.Company;
import com.dcode.jobconnect.entities.Job;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Optional<Job> findByJobId(String jobId);

    List<Job> findAllByPostedById(Long postedById);

        // Find jobs for a company
    List<Job> findByCompany(Company company);
    
    // Count jobs for a company and after a certain date
    long countByCompanyAndCreatedAtAfter(Company company, LocalDateTime date);

      boolean existsByJobId(String jobId);
    
    List<Job> findByCompanyId(Long companyId);


    List<Job> findByApplicationDeadlineBefore(LocalDateTime cutoffDate);


@Modifying
@Query("DELETE FROM Job j WHERE j.company.id = :companyId")
void deleteByCompanyId(@Param("companyId") Long companyId);

@Modifying
@Query(value = "DELETE FROM job_skills WHERE job_id IN (SELECT id FROM jobs WHERE company_id = :companyId)", nativeQuery = true)
void clearJobSkillsByCompanyId(@Param("companyId") Long companyId);

//  @Query("""
//         SELECT new JobDTO(
//             j.id, j.jobId, j.title, c.companyName, c.id, j.location,
//             j.jobType, j.experienceLevel, j.salaryRange, j.createdAt,
//             j.updatedAt, j.status
//         )
//         FROM Job j 
//         INNER JOIN j.company c
//         ORDER BY j.createdAt DESC
//         """)
//     Page<JobDTO> findAllJobsWithDetails(Pageable pageable);

}
