package com.DcoDe.jobconnect.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.DcoDe.jobconnect.entities.Job;

@Repository
public interface JobSearchRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

}
