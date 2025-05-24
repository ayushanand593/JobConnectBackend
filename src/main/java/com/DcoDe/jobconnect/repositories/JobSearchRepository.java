package com.dcode.jobconnect.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.dcode.jobconnect.entities.Job;

@Repository
public interface JobSearchRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

}
