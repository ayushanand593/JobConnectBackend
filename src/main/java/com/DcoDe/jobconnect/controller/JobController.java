package com.DcoDe.jobconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.DcoDe.jobconnect.dto.JobCreateDTO;
import com.DcoDe.jobconnect.dto.JobDTO;
import com.DcoDe.jobconnect.services.interfaces.JobServiceI;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController  {

    private final JobServiceI jobService;
    // @Autowired
    // private final ApplicationService applicationService;

    @PostMapping("/create-job")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    public ResponseEntity<JobDTO> createJob(@Valid @RequestBody JobCreateDTO jobDto) {
        JobDTO createdJob = jobService.createJob(jobDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdJob);
    }

     @GetMapping("/jobId/{jobId}")
    public ResponseEntity<JobDTO> getJobByJobId(@PathVariable String jobId) {
        return ResponseEntity.ok(jobService.getJobByJobId(jobId));
    }

       @PutMapping("/jobId/{jobId}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    public ResponseEntity<JobDTO> updateJobByJobId(
            @PathVariable String jobId,
            @Valid @RequestBody JobCreateDTO jobDto) {
        return ResponseEntity.ok(jobService.updateJobByJobId(jobId, jobDto));
    }

    @DeleteMapping("/jobId/{jobId}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    public ResponseEntity<String> deleteJobByJobId(@PathVariable String jobId) {
        jobService.deleteJobByJobId(jobId);
        return ResponseEntity.ok("Job deleted successfully");
    }
}
