package com.DcoDe.jobconnect.services.interfaces;

import java.util.List;

import com.DcoDe.jobconnect.entities.Candidate;
import com.DcoDe.jobconnect.entities.Job;
import com.DcoDe.jobconnect.entities.SavedJob;

public interface SavedJobServiceI {

   SavedJob saveJob(Long candidateId, String jobId);

   void unsaveJob(Long candidateId, String jobId);

   List<Job> getSavedJobsByCandidate(Long candidateId);

   boolean isJobSavedByCandidate(Long candidateId, String jobId);

   List<Candidate> getCandidatesWhoSavedJob(String jobId);
}
