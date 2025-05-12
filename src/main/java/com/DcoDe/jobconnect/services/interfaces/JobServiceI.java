package com.DcoDe.jobconnect.services.interfaces;

import com.DcoDe.jobconnect.dto.JobCreateDTO;
import com.DcoDe.jobconnect.dto.JobDTO;

public interface JobServiceI {

    JobDTO createJob(JobCreateDTO jobDto);

    JobDTO getJobByJobId(String jobId);

    JobDTO updateJobByJobId(String jobId, JobCreateDTO jobDto);

    void deleteJobByJobId(String jobId);
}
