package com.DcoDe.jobconnect.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

import com.DcoDe.jobconnect.dto.JobApplicationDTO;
import com.DcoDe.jobconnect.dto.JobApplicationSubmissionDTO;
import com.DcoDe.jobconnect.dto.JobCreateDTO;
import com.DcoDe.jobconnect.dto.JobDTO;
import com.DcoDe.jobconnect.enums.JobStatus;

public interface JobServiceI {

    JobDTO createJob(JobCreateDTO jobDto);

    JobDTO getJobByJobId(String jobId);

    JobDTO updateJobByJobId(String jobId, JobCreateDTO jobDto);

    void deleteJobByJobId(String jobId);
    
    JobApplicationDTO applyToJob(String jobId, JobApplicationSubmissionDTO applicationCreateDTO, MultipartFile resumeFile, MultipartFile coverLetterFile);

    void changeJobStatusByJobId(String jobId, JobStatus status);
}
