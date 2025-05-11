package com.DcoDe.jobconnect.services.interfaces;

import com.DcoDe.jobconnect.dto.CandidateProfileDTO;
import com.DcoDe.jobconnect.dto.CandidateProfileUpdateDTO;
import com.DcoDe.jobconnect.dto.CandidateRegistrationDTO;

public interface CandidateServiceI {
CandidateProfileDTO registerCandidate(CandidateRegistrationDTO dto);
CandidateProfileDTO getCurrentCandidateProfile();
CandidateProfileDTO getCandidateById(Long id);
CandidateProfileDTO updateCandidateProfile(CandidateProfileUpdateDTO profileDTO);
void deleteCandidateById(Long candidateId);
}
