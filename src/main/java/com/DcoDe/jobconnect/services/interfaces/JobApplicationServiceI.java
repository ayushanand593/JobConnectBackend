package com.DcoDe.jobconnect.services.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.DcoDe.jobconnect.dto.JobApplicationDTO;
import com.DcoDe.jobconnect.dto.JobApplicationSubmissionDTO;
import com.DcoDe.jobconnect.dto.JobApplicationUpdateDTO;
import com.DcoDe.jobconnect.enums.ApplicationStatus;

public interface JobApplicationServiceI {
        JobApplicationDTO submitApplication(JobApplicationSubmissionDTO submissionDTO,MultipartFile resumeFile,MultipartFile coverLetterFile);

        List<JobApplicationDTO> getApplicationsForJob(String jobId);

        JobApplicationDTO getJobApplication(Long id);

        Page<JobApplicationDTO> getCurrentCandidateApplications(Pageable pageable);

        JobApplicationDTO updateApplicationStatus(Long id, JobApplicationUpdateDTO updateDTO);

        void withdrawApplication(Long id);

        void updateApplicationStatus(Long id, ApplicationStatus status);

        public boolean isApplicationForEmployerJob(Long applicationId, Long employerId);

        boolean isApplicationForJobPostedByEmployer(Long applicationId, Long employerId); // Add this line
}
