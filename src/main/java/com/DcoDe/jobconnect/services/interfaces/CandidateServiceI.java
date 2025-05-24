package com.dcode.jobconnect.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

import com.dcode.jobconnect.dto.CandidateProfileDTO;
import com.dcode.jobconnect.dto.CandidateProfileUpdateDTO;
import com.dcode.jobconnect.dto.CandidateRegistrationDTO;
import com.dcode.jobconnect.entities.User;

public interface CandidateServiceI {
CandidateProfileDTO registerCandidate(CandidateRegistrationDTO dto);
CandidateProfileDTO getCurrentCandidateProfile();
CandidateProfileDTO getCandidateById(Long id);
CandidateProfileDTO updateCandidateProfile(CandidateProfileUpdateDTO profileDTO);
void deleteCandidateById(Long candidateId);

CandidateProfileDTO uploadResume(MultipartFile file);

 User findUserByEmail(String email);
}
