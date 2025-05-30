package com.dcode.jobconnect.controller;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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

import com.dcode.jobconnect.components.FileTypeValidator;
import com.dcode.jobconnect.dto.DisclosureQuestionDTO;
import com.dcode.jobconnect.dto.JobApplicationDTO;
import com.dcode.jobconnect.dto.JobApplicationSubmissionDTO;
import com.dcode.jobconnect.dto.JobCreateDTO;
import com.dcode.jobconnect.dto.JobDTO;
import com.dcode.jobconnect.dto.JobDisclosureQuestionsDTO;
import com.dcode.jobconnect.dto.JobSearchRequestDTO;
import com.dcode.jobconnect.dto.JobSearchResponseDTO;
import com.dcode.jobconnect.entities.Job;
import com.dcode.jobconnect.entities.User;
import com.dcode.jobconnect.enums.JobType;
import com.dcode.jobconnect.exceptions.ResourceNotFoundException;
import com.dcode.jobconnect.repositories.JobRepository;
import com.dcode.jobconnect.services.interfaces.JobSearchServiceI;
import com.dcode.jobconnect.services.interfaces.JobServiceI;
import com.dcode.jobconnect.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

    private final JobRepository jobRepository;  

    private String errMsg = "Job not found with jobId: ";
  


    @PostMapping("/create-job")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    @Operation(summary = "Create a new job posting")
    public ResponseEntity<JobDTO> createJob(@Valid @RequestBody JobCreateDTO jobDto) {
        JobDTO createdJob = jobService.createJob(jobDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdJob);
    }

@GetMapping("/jobs")
public ResponseEntity<Page<JobDTO>> getAllJobs(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
            
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<JobDTO> jobs = jobService.getAllJobs(pageable);
    return ResponseEntity.ok(jobs);
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
  User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authorized to update jobs");
        }

        Job job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException(errMsg + jobId));

        // Check if user has permission to update this job
        if (!job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Not authorized to update this job");
        }
            
        return ResponseEntity.ok(jobService.updateJobByJobId(jobId, jobDto));
    }

    @DeleteMapping("/jobId/{jobId}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER') or hasAuthority('EMPLOYER')")
    @Operation(summary = "Delete job by job ID")
    public ResponseEntity<String> deleteJobByJobId(@PathVariable String jobId) {

          User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authorized to update jobs");
        }

        Job job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException(errMsg + jobId));

        // Check if user has permission to update this job
        if (!job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Not authorized to update this job");
        }

        jobService.deleteJobByJobId(jobId);
        return ResponseEntity.ok("Job deleted successfully");
    }
  @PostMapping(value = "/apply/{jobId}",  consumes = {
    MediaType.MULTIPART_FORM_DATA_VALUE,
    MediaType.APPLICATION_OCTET_STREAM_VALUE
})
@PreAuthorize("hasAuthority('CANDIDATE')")
@Operation(
    summary = "Apply to a job",
    description = "Submit a job application with optional resume and cover letter files"
)
public ResponseEntity<JobApplicationDTO> applyToJob(
        @PathVariable 
        @Parameter(description = "Job ID to apply for", required = true) 
        String jobId,
        
        @RequestPart(value="applicationData") 
         @Parameter(
      description = "Job application data in JSON format",
      required = true,
      content = @Content(
        mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = JobApplicationSubmissionDTO.class)
      )
    )
        @Valid JobApplicationSubmissionDTO applicationCreateDTO,
        
        @RequestPart(value = "resumeFile", required = false)
        @Parameter(
            description = "Resume file (PDF, DOC, DOCX)", 
            required = false,
            content ={
                @Content(mediaType = "application/pdf", schema = @Schema(type = "string", format = "binary")),
                @Content(mediaType = "application/text", schema = @Schema(type = "string", format = "binary")),
                @Content(mediaType = "application/msword", schema = @Schema(type = "string", format = "binary")),
            }
        )
        MultipartFile resumeFile,
        
        @RequestPart(value = "coverLetterFile", required = false)
        @Parameter(
            description = "Cover letter file (PDF, DOC, DOCX)", 
            required = false,
             content ={
                @Content(mediaType = "application/pdf", schema = @Schema(type = "string", format = "binary")),
                @Content(mediaType = "application/text", schema = @Schema(type = "string", format = "binary")),
                @Content(mediaType = "application/msword", schema = @Schema(type = "string", format = "binary")),
            }
        )
        MultipartFile coverLetterFile) {

            // 1) Validate file parts
    List<String> allowedTypes = List.of(
      "application/pdf",
      "text/plain",
      "application/msword",
      // docx
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

     FileTypeValidator.validateMimeType(resumeFile,      allowedTypes, "resumeFile");
    FileTypeValidator.validateMimeType(coverLetterFile, allowedTypes, "coverLetterFile");

       
    
    User currentUser = SecurityUtils.getCurrentUser();
    if (currentUser == null) {
        throw new AccessDeniedException("Must be logged in to apply for a job");
    }
    
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
          User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authorized to view disclosure questions");
        }

        Job job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException(errMsg + jobId));

        // Check if user has permission to see disclosures for this job
        if (!job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Not authorized to view disclosure questions for this job");
        }
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
          User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Not authorized to update jobs");
        }

        Job job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException(errMsg + jobId));

        // Check if user has permission to update this job
        if (!job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Not authorized to update this job");
        }
        JobDTO updatedJob = jobService.updateJobDisclosureQuestions(jobId, questions);
        return ResponseEntity.ok(updatedJob);
    }

    
}

   

