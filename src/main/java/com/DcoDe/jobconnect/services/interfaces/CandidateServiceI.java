package com.DcoDe.jobconnect.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

import com.DcoDe.jobconnect.dto.CandidateProfileDTO;
import com.DcoDe.jobconnect.dto.CandidateProfileUpdateDTO;
import com.DcoDe.jobconnect.dto.CandidateRegistrationDTO;
import com.DcoDe.jobconnect.entities.User;

public interface CandidateServiceI {
CandidateProfileDTO registerCandidate(CandidateRegistrationDTO dto);
CandidateProfileDTO getCurrentCandidateProfile();
CandidateProfileDTO getCandidateById(Long id);
CandidateProfileDTO updateCandidateProfile(CandidateProfileUpdateDTO profileDTO);
void deleteCandidateById(Long candidateId);

CandidateProfileDTO uploadResume(MultipartFile file);

 User findUserByEmail(String email);
}
