package com.DcoDe.jobconnect.services;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.DcoDe.jobconnect.dto.JobSearchRequestDTO;
import com.DcoDe.jobconnect.dto.JobSearchResponseDTO;
import com.DcoDe.jobconnect.entities.Job;
import com.DcoDe.jobconnect.entities.Skill;
import com.DcoDe.jobconnect.repositories.JobSearchRepository;
import com.DcoDe.jobconnect.services.interfaces.JobSearchServiceI;
import com.DcoDe.jobconnect.specifications.JobSpecification;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobSearchServiceImpl implements JobSearchServiceI {

    private final JobSearchRepository jobRepository;
    
    @Override
    public Page<JobSearchResponseDTO> searchJobs(JobSearchRequestDTO searchRequest) {
        // Create pageable for pagination and sorting
        Sort sort = Sort.by(
                searchRequest.getSortDirection().equalsIgnoreCase("ASC") ? 
                Sort.Direction.ASC : Sort.Direction.DESC, 
                searchRequest.getSortBy()
        );
        
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(), 
                searchRequest.getSize(), 
                sort
        );

        // Execute search with specifications
        Page<Job> jobPage = jobRepository.findAll(
                JobSpecification.searchJobs(searchRequest),
                pageable
        );

        // Map results to DTOs
        return jobPage.map(this::convertToJobSearchResponseDTO);
    }

    private JobSearchResponseDTO convertToJobSearchResponseDTO(Job job) {
        return JobSearchResponseDTO.builder()
                .id(job.getId())
                .jobId(job.getJobId())
                .title(job.getTitle())
                .companyName(job.getCompany().getCompanyName())
                .companyLogoUrl(job.getCompany().getLogoUrl())
                .location(job.getLocation())
                .jobType(job.getJobType())
                .experienceLevel(job.getExperienceLevel())
                .salaryRange(job.getSalaryRange())
                .skills(job.getSkills().stream()
                        .map(Skill::getName)
                        .collect(Collectors.toSet()))
                .createdAt(job.getCreatedAt())
                .build();
    }

}
