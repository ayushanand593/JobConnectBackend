package com.dcode.jobconnect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.dcode.jobconnect.controller.JobApplicationController;
import com.dcode.jobconnect.dto.JobApplicationDTO;
import com.dcode.jobconnect.dto.JobApplicationSubmissionDTO;
import com.dcode.jobconnect.dto.JobApplicationUpdateDTO;
import com.dcode.jobconnect.enums.ApplicationStatus;
import com.dcode.jobconnect.services.interfaces.JobApplicationServiceI;
import com.dcode.jobconnect.utils.SecurityUtils;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationControllerTest {

    @Mock
    private JobApplicationServiceI jobApplicationService;

    @InjectMocks
    private JobApplicationController controller;

    private JobApplicationDTO sampleDto;
    private JobApplicationSubmissionDTO submissionDto;
    private JobApplicationUpdateDTO updateDto;

    @BeforeEach
    void setup() {
        sampleDto = new JobApplicationDTO();
        sampleDto.setId(1L);
        sampleDto.setCandidateId(100L);
        sampleDto.setJobId(200L);

        submissionDto = new JobApplicationSubmissionDTO();
        submissionDto.setJobId(200L);
        // submissionDto.setCandidateId(100L);
        submissionDto.setVoluntaryDisclosures("Answer1");

        updateDto = new JobApplicationUpdateDTO();
        updateDto.setStatus(ApplicationStatus.SUBMITTED);
    }

    @Test
    void submitApplication_Success() {
        MultipartFile resume = new MockMultipartFile("resume", "res.pdf", "application/pdf", "data".getBytes());
        MultipartFile cover = new MockMultipartFile("cover", "cov.pdf", "application/pdf", "data".getBytes());

        try (MockedStatic<SecurityUtils> utils = Mockito.mockStatic(SecurityUtils.class)) {
            // authenticated candidate
            com.dcode.jobconnect.entities.User user = new com.dcode.jobconnect.entities.User();
            user.setId(100L);
            user.setRole(com.dcode.jobconnect.enums.UserRole.CANDIDATE);
            utils.when(SecurityUtils::getCurrentUser).thenReturn(user);

            when(jobApplicationService.submitApplication(eq(submissionDto), eq(resume), eq(cover)))
                .thenReturn(sampleDto);

            ResponseEntity<JobApplicationDTO> resp = controller.submitApplication(submissionDto, resume, cover);
            assertEquals(200, resp.getStatusCodeValue());
            assertEquals(sampleDto, resp.getBody());
        }
    }

    @Test
    void submitApplication_NotAuthenticated() {
        try (MockedStatic<SecurityUtils> utils = Mockito.mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUser).thenReturn(null);
            assertThrows(AccessDeniedException.class, () ->
                controller.submitApplication(submissionDto, null, null)
            );
        }
    }

    @Test
    void getApplication_CandidateAccess() {
        try (MockedStatic<SecurityUtils> utils = Mockito.mockStatic(SecurityUtils.class)) {
            com.dcode.jobconnect.entities.User user = new com.dcode.jobconnect.entities.User();
            user.setId(100L);
            user.setRole(com.dcode.jobconnect.enums.UserRole.CANDIDATE);
            utils.when(SecurityUtils::getCurrentUser).thenReturn(user);

            when(jobApplicationService.getJobApplication(1L)).thenReturn(sampleDto);

            ResponseEntity<JobApplicationDTO> resp = controller.getApplication(1L);
            assertEquals(200, resp.getStatusCodeValue());
            assertEquals(sampleDto, resp.getBody());
        }
    }

    @Test
    void getApplication_EmployerAccess() {
        try (MockedStatic<SecurityUtils> utils = Mockito.mockStatic(SecurityUtils.class)) {
            com.dcode.jobconnect.entities.User user = new com.dcode.jobconnect.entities.User();
            user.setId(500L);
            user.setRole(com.dcode.jobconnect.enums.UserRole.EMPLOYER);
            utils.when(SecurityUtils::getCurrentUser).thenReturn(user);

            when(jobApplicationService.getJobApplication(1L)).thenReturn(sampleDto);
            when(jobApplicationService.isApplicationForEmployerJob(1L, 500L)).thenReturn(true);

            ResponseEntity<JobApplicationDTO> resp = controller.getApplication(1L);
            assertEquals(200, resp.getStatusCodeValue());
            assertEquals(sampleDto, resp.getBody());
        }
    }

    @Test
    void getApplication_Forbidden() {
        try (MockedStatic<SecurityUtils> utils = Mockito.mockStatic(SecurityUtils.class)) {
            com.dcode.jobconnect.entities.User user = new com.dcode.jobconnect.entities.User();
            user.setId(300L);
            user.setRole(com.dcode.jobconnect.enums.UserRole.CANDIDATE);
            utils.when(SecurityUtils::getCurrentUser).thenReturn(user);

            when(jobApplicationService.getJobApplication(1L)).thenReturn(sampleDto);

            assertThrows(AccessDeniedException.class, () -> controller.getApplication(1L));
        }
    }

    @Test
    void getMyApplications_Success() {
        try (MockedStatic<SecurityUtils> utils = Mockito.mockStatic(SecurityUtils.class)) {
            com.dcode.jobconnect.entities.User user = new com.dcode.jobconnect.entities.User();
            user.setId(100L);
            user.setRole(com.dcode.jobconnect.enums.UserRole.CANDIDATE);
            utils.when(SecurityUtils::getCurrentUser).thenReturn(user);

            Page<JobApplicationDTO> page = new PageImpl<>(Collections.singletonList(sampleDto), PageRequest.of(0,10), 1);
            when(jobApplicationService.getCurrentCandidateApplications(any())).thenReturn(page);

            ResponseEntity<Page<JobApplicationDTO>> resp = controller.getMyApplications(0, 10);
            assertEquals(200, resp.getStatusCodeValue());
            assertEquals(1, resp.getBody().getTotalElements());
        }
    }

    @Test
    void updateApplicationStatus_Success() {
        try (MockedStatic<SecurityUtils> utils = Mockito.mockStatic(SecurityUtils.class)) {
            com.dcode.jobconnect.entities.User user = new com.dcode.jobconnect.entities.User();
            user.setId(500L);
            user.setRole(com.dcode.jobconnect.enums.UserRole.EMPLOYER);
            utils.when(SecurityUtils::getCurrentUser).thenReturn(user);

            when(jobApplicationService.isApplicationForEmployerJob(1L, 500L)).thenReturn(true);
            when(jobApplicationService.updateApplicationStatus(1L, updateDto)).thenReturn(sampleDto);

            ResponseEntity<JobApplicationDTO> resp = controller.updateApplicationStatus(1L, updateDto);
            assertEquals(200, resp.getStatusCodeValue());
            assertEquals(sampleDto, resp.getBody());
        }
    }

    @Test
    void updateApplicationStatus_Forbidden() {
        try (MockedStatic<SecurityUtils> utils = Mockito.mockStatic(SecurityUtils.class)) {
            com.dcode.jobconnect.entities.User user = new com.dcode.jobconnect.entities.User();
            user.setId(500L);
            user.setRole(com.dcode.jobconnect.enums.UserRole.EMPLOYER);
            utils.when(SecurityUtils::getCurrentUser).thenReturn(user);

            when(jobApplicationService.isApplicationForEmployerJob(1L, 500L)).thenReturn(false);
            assertThrows(AccessDeniedException.class, () -> controller.updateApplicationStatus(1L, updateDto));
        }
    }

    // @Test
    // void withdrawApplication_Success() {
    //     try (MockedStatic<SecurityUtils> utils = Mockito.mockStatic(SecurityUtils.class)) {
    //         com.DcoDe.jobconnect.entities.User user = new com.DcoDe.jobconnect.entities.User();
    //         user.setId(100L);
    //         user.setRole(com.DcoDe.jobconnect.enums.UserRole.CANDIDATE);
    //         utils.when(SecurityUtils::getCurrentUser).thenReturn(user);

    //         when(jobApplicationService.getJobApplication(1L)).thenReturn(sampleDto);
    //         doNothing().when(jobApplicationService).withdrawApplication(1L);

    //         ResponseEntity<Void> resp = controller.withdrawApplication(1L);
    //         assertEquals(204, resp.getStatusCodeValue());
    //     }
    // }

    // @Test
    // void withdrawApplication_Forbidden() {
    //     try (MockedStatic<SecurityUtils> utils = Mockito.mockStatic(SecurityUtils.class)) {
    //         com.DcoDe.jobconnect.entities.User user = new com.DcoDe.jobconnect.entities.User();
    //         user.setId(999L);
    //         user.setRole(com.DcoDe.jobconnect.enums.UserRole.CANDIDATE);
    //         utils.when(SecurityUtils::getCurrentUser).thenReturn(user);

    //         when(jobApplicationService.getJobApplication(1L)).thenReturn(sampleDto);
    //         assertThrows(AccessDeniedException.class, () -> controller.withdrawApplication(1L));
    //     }
    // }
}

