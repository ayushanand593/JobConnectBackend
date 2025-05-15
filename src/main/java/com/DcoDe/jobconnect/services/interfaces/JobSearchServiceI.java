package com.DcoDe.jobconnect.services.interfaces;

import org.springframework.data.domain.Page;

import com.DcoDe.jobconnect.dto.JobSearchRequestDTO;
import com.DcoDe.jobconnect.dto.JobSearchResponseDTO;

public interface JobSearchServiceI {
Page<JobSearchResponseDTO> searchJobs(JobSearchRequestDTO searchRequest);
}
