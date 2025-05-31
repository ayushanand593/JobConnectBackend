package com.dcode.jobconnect.services;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.dcode.jobconnect.dto.JobSearchRequestDTO;
import com.dcode.jobconnect.dto.JobSearchResponseDTO;
import com.dcode.jobconnect.entities.Job;
import com.dcode.jobconnect.entities.Skill;
import com.dcode.jobconnect.repositories.JobSearchRepository;
import com.dcode.jobconnect.services.interfaces.FileStorageServiceI;
import com.dcode.jobconnect.services.interfaces.JobSearchServiceI;
import com.dcode.jobconnect.specifications.JobSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobSearchServiceImpl implements JobSearchServiceI {

    private final JobSearchRepository jobRepository;
    private final FileStorageServiceI fileStorageService;  // Inject FileStorageServiceI
   
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
        // Attempt to get detailed logo info
        FileStorageServiceImpl.LogoInfo logoInfo = fileStorageService.getCompanyLogoInfo(job.getCompany().getId());

        return JobSearchResponseDTO.builder()
                .id(job.getId())
                .jobId(job.getJobId())
                .title(job.getTitle())
                .companyName(job.getCompany().getCompanyName())
                .companyLogoUrl(job.getCompany().getLogoUrl()) // if applicable; or leave blank if logoInfo is used
                // Populate new logo fields if available
                .logoFileId(logoInfo != null ? logoInfo.getFileId() : null)
                .logoBase64(logoInfo != null ? logoInfo.getBase64Data() : null)
                .logoContentType(logoInfo != null ? logoInfo.getContentType() : null)
                .logoFileName(logoInfo != null ? logoInfo.getFileName() : null)
                .logoDataUrl(logoInfo != null ? logoInfo.getDataUrl() : null)
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
