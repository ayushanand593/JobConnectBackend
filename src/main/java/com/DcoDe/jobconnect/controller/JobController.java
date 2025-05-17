package com.DcoDe.jobconnect.controller;
import java.util.List;

import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.DcoDe.jobconnect.dto.DisclosureQuestionDTO;
import com.DcoDe.jobconnect.dto.JobApplicationDTO;
import com.DcoDe.jobconnect.dto.JobApplicationSubmissionDTO;
import com.DcoDe.jobconnect.dto.JobCreateDTO;
import com.DcoDe.jobconnect.dto.JobDTO;
import com.DcoDe.jobconnect.dto.JobDisclosureQuestionsDTO;
import com.DcoDe.jobconnect.dto.JobSearchRequestDTO;
import com.DcoDe.jobconnect.dto.JobSearchResponseDTO;
import com.DcoDe.jobconnect.enums.JobType;
import com.DcoDe.jobconnect.services.interfaces.JobSearchServiceI;
import com.DcoDe.jobconnect.services.interfaces.JobServiceI;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Job", description = "API for managing job postings and applications.")
public class JobController  {

    private final JobServiceI jobService;

    private final JobSearchServiceI jobSearchService;
    // @Autowired
    // private final ApplicationService applicationService;


    

    @PostMapping("/create-job")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    @Operation(summary = "Create a new job posting")
    public ResponseEntity<JobDTO> createJob(@Valid @RequestBody JobCreateDTO jobDto) {
        JobDTO createdJob = jobService.createJob(jobDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdJob);
    }

     @GetMapping("/jobId/{jobId}")
     @Operation(summary = "Get job details by job ID")
    public ResponseEntity<JobDTO> getJobByJobId(@PathVariable String jobId) {
        return ResponseEntity.ok(jobService.getJobByJobId(jobId));
    }

       @PutMapping("/jobId/{jobId}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    @Operation(summary = "Update job details by job ID")
    public ResponseEntity<JobDTO> updateJobByJobId(
            @PathVariable String jobId,
            @Valid @RequestBody JobCreateDTO jobDto) {
        return ResponseEntity.ok(jobService.updateJobByJobId(jobId, jobDto));
    }

    @DeleteMapping("/jobId/{jobId}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    @Operation(summary = "Delete job by job ID")
    public ResponseEntity<String> deleteJobByJobId(@PathVariable String jobId) {
        jobService.deleteJobByJobId(jobId);
        return ResponseEntity.ok("Job deleted successfully");
    }
    @PostMapping("/apply/{jobId}")
    @PreAuthorize("hasAuthority('CANDIDATE')")
    @Operation(summary = "Apply to a job")
    public ResponseEntity<JobApplicationDTO> applyToJob(
            @PathVariable String jobId,
            @RequestPart("applicationData") @Valid JobApplicationSubmissionDTO applicationCreateDTO,
            @RequestPart(value = "resumeFile", required = false) MultipartFile resumeFile,
            @RequestPart(value = "coverLetterFile", required = false) MultipartFile coverLetterFile) {
        JobApplicationDTO applicationDTO = jobService.applyToJob(jobId, applicationCreateDTO, resumeFile, coverLetterFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationDTO);
    }

     @PostMapping("/search")
     @Operation(summary = "Search for jobs")
    public ResponseEntity<Page<JobSearchResponseDTO>> searchJobs(@RequestBody JobSearchRequestDTO searchRequest) {
        Page<JobSearchResponseDTO> results = jobSearchService.searchJobs(searchRequest);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search for jobs using query parameters")
    public ResponseEntity<Page<JobSearchResponseDTO>> searchJobsGet(
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) List<String> skills,
            @RequestParam(required = false) String experienceLevel,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {
        
        JobSearchRequestDTO searchRequest = new JobSearchRequestDTO();
        searchRequest.setCompanyName(companyName);
        searchRequest.setJobTitle(jobTitle);
        searchRequest.setLocation(location);
        searchRequest.setSkills(skills);
        searchRequest.setExperienceLevel(experienceLevel);
        searchRequest.setJobType(jobType);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);
        
        Page<JobSearchResponseDTO> results = jobSearchService.searchJobs(searchRequest);
        return ResponseEntity.ok(results);
    }


    /**
     * Get disclosure questions for a job
     */
    @GetMapping("/{jobId}/disclosure-questions")
    @Operation(summary = "Get disclosure questions for a job")
    public ResponseEntity<JobDisclosureQuestionsDTO> getJobDisclosureQuestions(@PathVariable String jobId) {
        JobDisclosureQuestionsDTO questions = jobService.getJobDisclosureQuestions(jobId);
        return ResponseEntity.ok(questions);
    }
    
    /**
     * Add or update disclosure questions for a job (for employers)
     */
    @PostMapping("/{jobId}/disclosure-questions")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Add or update disclosure questions for a job")
    public ResponseEntity<JobDTO> updateJobDisclosureQuestions(
            @PathVariable String jobId, 
            @RequestBody List<DisclosureQuestionDTO> questions) {
        JobDTO updatedJob = jobService.updateJobDisclosureQuestions(jobId, questions);
        return ResponseEntity.ok(updatedJob);
    }

   
}
