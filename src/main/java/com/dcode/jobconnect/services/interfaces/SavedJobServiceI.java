package com.dcode.jobconnect.services.interfaces;

import java.util.List;

import com.dcode.jobconnect.entities.Job;
import com.dcode.jobconnect.entities.SavedJob;

public interface SavedJobServiceI {

   SavedJob saveJob(Long candidateId, String jobId);

   void unsaveJob(Long candidateId, String jobId);

   List<Job> getSavedJobsByCandidate(Long candidateId);

   boolean isJobSavedByCandidate(Long candidateId, String jobId);
}
