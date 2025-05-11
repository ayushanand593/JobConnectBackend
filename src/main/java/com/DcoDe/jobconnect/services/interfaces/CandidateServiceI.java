package com.DcoDe.jobconnect.services.interfaces;

import com.DcoDe.jobconnect.dto.CandidateProfileDTO;
import com.DcoDe.jobconnect.dto.CandidateRegistrationDTO;

public interface CandidateServiceI {
CandidateProfileDTO registerCandidate(CandidateRegistrationDTO dto);
CandidateProfileDTO getCurrentCandidateProfile();
}
