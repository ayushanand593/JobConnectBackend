package com.dcode.jobconnect.services.interfaces;

import org.springframework.data.domain.Page;

import com.dcode.jobconnect.dto.JobSearchRequestDTO;
import com.dcode.jobconnect.dto.JobSearchResponseDTO;

public interface JobSearchServiceI {
Page<JobSearchResponseDTO> searchJobs(JobSearchRequestDTO searchRequest);
}
