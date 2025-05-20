package com.DcoDe.jobconnect.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.DcoDe.jobconnect.entities.Candidate;
import com.DcoDe.jobconnect.entities.Job;
import com.DcoDe.jobconnect.entities.SavedJob;
import com.DcoDe.jobconnect.repositories.CandidateRepository;
import com.DcoDe.jobconnect.repositories.JobRepository;
import com.DcoDe.jobconnect.repositories.SavedJobRepository;
import com.DcoDe.jobconnect.services.interfaces.SavedJobServiceI;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavedJobServiceImpl implements SavedJobServiceI {
    
 private final SavedJobRepository savedJobRepository;
    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;

    // Constructor with autowired dependencies

    @Override
    @Transactional
    public SavedJob saveJob(Long candidateId, String jobId) {
        Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new NoSuchElementException("Candidate not found"));
        
        Job job = jobRepository.findByJobId(jobId)
            .orElseThrow(() -> new NoSuchElementException("Job not found"));
        
        // Check if already saved
        if (savedJobRepository.existsByCandidateAndJob(candidate, job)) {
            throw new IllegalStateException("Job already saved by this candidate");
        }
        
        SavedJob savedJob = new SavedJob();
        savedJob.setCandidate(candidate);
        savedJob.setJob(job);
        savedJob.setSavedAt(LocalDateTime.now());
        
        return savedJobRepository.save(savedJob);
    }

    @Override
    @Transactional
    public void unsaveJob(Long candidateId, String jobId) {
        Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new NoSuchElementException("Candidate not found"));
        
        Job job = jobRepository.findByJobId(jobId)
            .orElseThrow(() -> new NoSuchElementException("Job not found"));
        
        SavedJob savedJob = savedJobRepository.findByCandidateAndJob(candidate, job)
            .orElseThrow(() -> new NoSuchElementException("Saved job not found"));
        
        savedJobRepository.delete(savedJob);
    }

     @Override
    public List<Job> getSavedJobsByCandidate(Long candidateId) {
        List<SavedJob> savedJobs = savedJobRepository.findByCandidateId(candidateId);
        return savedJobs.stream()
            .map(SavedJob::getJob)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isJobSavedByCandidate(Long candidateId, String jobId) {
        Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new NoSuchElementException("Candidate not found"));
        
        Job job = jobRepository.findByJobId(jobId)
            .orElseThrow(() -> new NoSuchElementException("Job not found"));
        
        return savedJobRepository.existsByCandidateAndJob(candidate, job);
    }

 
}
