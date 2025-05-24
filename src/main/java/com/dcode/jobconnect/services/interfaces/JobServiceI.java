package com.dcode.jobconnect.services.interfaces;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.dcode.jobconnect.dto.DisclosureQuestionDTO;
import com.dcode.jobconnect.dto.JobApplicationDTO;
import com.dcode.jobconnect.dto.JobApplicationSubmissionDTO;
import com.dcode.jobconnect.dto.JobCreateDTO;
import com.dcode.jobconnect.dto.JobDTO;
import com.dcode.jobconnect.dto.JobDisclosureQuestionsDTO;
import com.dcode.jobconnect.entities.Job;
import com.dcode.jobconnect.enums.JobStatus;

public interface JobServiceI {

    JobDTO createJob(JobCreateDTO jobDto);

    JobDTO getJobByJobId(String jobId);

    JobDTO updateJobByJobId(String jobId, JobCreateDTO jobDto);

    void deleteJobByJobId(String jobId);
    
    JobApplicationDTO applyToJob(String jobId, JobApplicationSubmissionDTO applicationCreateDTO, MultipartFile resumeFile, MultipartFile coverLetterFile);

    void changeJobStatusByJobId(String jobId, JobStatus status);

    JobDisclosureQuestionsDTO getJobDisclosureQuestions(String jobId);

    JobDTO updateJobDisclosureQuestions(String jobId, List<DisclosureQuestionDTO> questions);

    public Job getJobEntityByJobId(String jobId);
}
