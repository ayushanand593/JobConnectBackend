package com.DcoDe.jobconnect;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import com.DcoDe.jobconnect.controller.CandidateController;
import com.DcoDe.jobconnect.controller.JobApplicationController;
import com.DcoDe.jobconnect.dto.CandidateDashboardStatsDTO;
import com.DcoDe.jobconnect.dto.CandidateProfileDTO;
import com.DcoDe.jobconnect.dto.CandidateProfileUpdateDTO;
import com.DcoDe.jobconnect.dto.CandidateRegistrationDTO;
import com.DcoDe.jobconnect.dto.JobApplicationDetailDTO;
import com.DcoDe.jobconnect.dto.JwtResponseDTO;
import com.DcoDe.jobconnect.dto.UserDTO;
import com.DcoDe.jobconnect.entities.User;
import com.DcoDe.jobconnect.enums.UserRole;
import com.DcoDe.jobconnect.services.interfaces.AuthServiceI;
import com.DcoDe.jobconnect.services.interfaces.CandidateServiceI;
import com.DcoDe.jobconnect.services.interfaces.DashboardServiceI;
import com.DcoDe.jobconnect.services.interfaces.JobApplicationServiceI;
import com.DcoDe.jobconnect.utils.SecurityUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateControllerTest {

    @Mock
    private CandidateServiceI candidateService;

    @Mock
    private DashboardServiceI dashboardService;

    @Mock
    private JobApplicationServiceI jobApplicationService;

    @Mock
    private AuthServiceI authService;

    @InjectMocks
    private CandidateController candidateController;

    private User mockUser;
    private CandidateRegistrationDTO registrationDTO;
    private CandidateProfileDTO profileDTO;
    private CandidateProfileUpdateDTO updateDTO;
    private JwtResponseDTO jwtResponseDTO;

    @BeforeEach
    void setUp() {
        // Setup mock user
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setRole(UserRole.CANDIDATE);

        // Setup registration DTO
        registrationDTO = new CandidateRegistrationDTO();
        registrationDTO.setEmail("test@example.com");
        registrationDTO.setPassword("password123");
        registrationDTO.setFirstName("John");
        registrationDTO.setLastName("Doe");
        registrationDTO.setPhone("1234567890");
        registrationDTO.setHeadline("Software Developer");
        registrationDTO.setSummary("Experienced developer");
        registrationDTO.setExperienceYears(5);
        registrationDTO.setSkills(Arrays.asList("Java", "Spring Boot"));

        // Setup profile DTO
        profileDTO = new CandidateProfileDTO();
        profileDTO.setId(1L);
        profileDTO.setEmail("test@example.com");
        profileDTO.setFirstName("John");
        profileDTO.setLastName("Doe");

        // Setup user dto
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("abc@example.com");
        userDTO.setRole("CANDIDATE");
        userDTO.setCompanyID(1L);

        // Setup update DTO
        updateDTO = new CandidateProfileUpdateDTO();
        updateDTO.setFirstName("John");
        updateDTO.setLastName("Doe");
        updateDTO.setPhone("1234567890");
        updateDTO.setHeadline("Senior Software Developer");
        updateDTO.setSummary("Experienced developer with leadership skills");
        updateDTO.setExperienceYears(6);
        updateDTO.setSkills(Arrays.asList("Java", "Spring Boot", "AWS"));

        // Setup JWT response
        jwtResponseDTO = new JwtResponseDTO();
        jwtResponseDTO.setToken("mock-jwt-token");
        jwtResponseDTO.setTokenType("Bearer");
        jwtResponseDTO.setUser(userDTO);
    }

    @Test
    void testRegisterCandidate_Success() {
        // Arrange
        when(candidateService.registerCandidate(any(CandidateRegistrationDTO.class))).thenReturn(profileDTO);
        when(candidateService.findUserByEmail(anyString())).thenReturn(mockUser);
        when(authService.generateTokenForUser(any(User.class))).thenReturn(jwtResponseDTO);

        // Act
        ResponseEntity<JwtResponseDTO> response = candidateController.registerCandidate(registrationDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("mock-jwt-token", response.getBody().getToken());
        verify(candidateService).registerCandidate(registrationDTO);
        verify(candidateService).findUserByEmail("test@example.com");
        verify(authService).generateTokenForUser(mockUser);
    }

    @Test
    void testRegisterCandidate_Exception() {
        // Arrange
        when(candidateService.registerCandidate(any(CandidateRegistrationDTO.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            candidateController.registerCandidate(registrationDTO);
        });
        
        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void testGetCurrentProfile_Success() {
        // Arrange
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(candidateService.getCurrentCandidateProfile()).thenReturn(profileDTO);

            // Act
            ResponseEntity<CandidateProfileDTO> response = candidateController.getCurrentProfile();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("test@example.com", response.getBody().getEmail());
            verify(candidateService).getCurrentCandidateProfile();
        }
    }

    @Test
    void testGetCurrentProfile_NotAuthenticated() {
        // Arrange
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(null);

            // Act & Assert
            assertThrows(AccessDeniedException.class, () -> {
                candidateController.getCurrentProfile();
            });
        }
    }

    @Test
    void testGetCandidateById_OwnProfile_Success() {
        // Arrange
        Long candidateId = 1L;
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(candidateService.getCandidateById(candidateId)).thenReturn(profileDTO);

            // Act
            ResponseEntity<CandidateProfileDTO> response = candidateController.getCandidateById(candidateId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("test@example.com", response.getBody().getEmail());
            verify(candidateService).getCandidateById(candidateId);
        }
    }

    @Test
    void testGetCandidateById_DifferentProfile_Forbidden() {
        // Arrange
        Long candidateId = 2L; // Different ID than logged in user
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            // Act & Assert
            assertThrows(AccessDeniedException.class, () -> {
                candidateController.getCandidateById(candidateId);
            });
        }
    }

    @Test
    void testGetCandidateById_Admin_Success() {
        // Arrange
        Long candidateId = 2L; // Different ID than logged in user
        User adminUser = new User();
        adminUser.setId(3L);
        adminUser.setRole(UserRole.ADMIN);
        
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(adminUser);
            when(candidateService.getCandidateById(candidateId)).thenReturn(profileDTO);

            // Act
            ResponseEntity<CandidateProfileDTO> response = candidateController.getCandidateById(candidateId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(candidateService).getCandidateById(candidateId);
        }
    }

    @Test
    void testUpdateProfile_Success() {
        // Arrange
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(candidateService.updateCandidateProfile(any(CandidateProfileUpdateDTO.class))).thenReturn(profileDTO);

            // Act
            ResponseEntity<CandidateProfileDTO> response = candidateController.updateProfile(updateDTO);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(candidateService).updateCandidateProfile(updateDTO);
        }
    }

    @Test
    void testUpdateProfile_NotAuthenticated() {
        // Arrange
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(null);

            // Act & Assert
            assertThrows(AccessDeniedException.class, () -> {
                candidateController.updateProfile(updateDTO);
            });
        }
    }

    @Test
    void testDeleteCandidate_OwnProfile_Success() {
        // Arrange
        Long candidateId = 1L;
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            doNothing().when(candidateService).deleteCandidateById(candidateId);

            // Act
            ResponseEntity<String> response = candidateController.deleteCandidate(candidateId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Candidate deleted successfully.", response.getBody());
            verify(candidateService).deleteCandidateById(candidateId);
        }
    }

    @Test
    void testDeleteCandidate_DifferentProfile_Forbidden() {
        // Arrange
        Long candidateId = 2L; // Different ID than logged in user
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            // Act & Assert
            assertThrows(AccessDeniedException.class, () -> {
                candidateController.deleteCandidate(candidateId);
            });
        }
    }

    // @Test
    // void testUploadResume_Success() {
    //     // Arrange
    //     MultipartFile file = new MockMultipartFile(
    //         "file", "resume.pdf", "application/pdf", "PDF content".getBytes());
        
    //     try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
    //         securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
    //         when(candidateService.uploadResume(any(MultipartFile.class))).thenReturn(profileDTO);

    //         // Act
    //         ResponseEntity<CandidateProfileDTO> response = candidateController.uploadResume(file);

    //         // Assert
    //         assertEquals(HttpStatus.OK, response.getStatusCode());
    //         assertNotNull(response.getBody());
    //         verify(candidateService).uploadResume(file);
    //     }
    // }

    @Test
    void testUploadResume_NotAuthenticated() {
        // Arrange
        MultipartFile file = new MockMultipartFile(
            "file", "resume.pdf", "application/pdf", "PDF content".getBytes());
        
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(null);

            // Act & Assert
            assertThrows(AccessDeniedException.class, () -> {
                candidateController.uploadResume(file);
            });
        }
    }

    @Test
    void testGetCandidateDashboardStats_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        CandidateDashboardStatsDTO statsDTO = new CandidateDashboardStatsDTO();
        
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(dashboardService.getCandidateDashboardStats(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(statsDTO);

            // Act
            ResponseEntity<CandidateDashboardStatsDTO> response = 
                candidateController.getCandidateDashboardStats(startDate, endDate);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(dashboardService).getCandidateDashboardStats(startDate, endDate);
        }
    }

    @Test
    void testGetCandidateDashboardStats_DefaultDates() {
        // Arrange
        CandidateDashboardStatsDTO statsDTO = new CandidateDashboardStatsDTO();
        
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(dashboardService.getCandidateDashboardStats(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(statsDTO);

            // Act
            ResponseEntity<CandidateDashboardStatsDTO> response = 
                candidateController.getCandidateDashboardStats(null, null);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(dashboardService).getCandidateDashboardStats(any(LocalDate.class), any(LocalDate.class));
        }
    }

    @Test
    void testGetCandidateApplications_Success() {
        // Arrange
        List<JobApplicationDetailDTO> applications = new ArrayList<>();
        JobApplicationDetailDTO app = new JobApplicationDetailDTO();
        app.setId(1L);
        applications.add(app);
        
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(dashboardService.getCandidateApplications()).thenReturn(applications);

            // Act
            ResponseEntity<List<JobApplicationDetailDTO>> response = candidateController.getCandidateApplications();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            verify(dashboardService).getCandidateApplications();
        }
    }

    @Test
    void testGetCandidateApplicationDetail_Success() {
        // Arrange
        Long applicationId = 1L;
        JobApplicationDetailDTO applicationDTO = new JobApplicationDetailDTO();
        applicationDTO.setId(applicationId);
        
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(dashboardService.getCandidateApplicationDetail(applicationId)).thenReturn(applicationDTO);

            // Act
            ResponseEntity<JobApplicationDetailDTO> response = 
                candidateController.getCandidateApplicationDetail(applicationId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(applicationId, response.getBody().getId());
            verify(dashboardService).getCandidateApplicationDetail(applicationId);
        }
    }

    @Test
    void testGetCandidateApplicationDetail_NotFound() {
        // Arrange
        Long applicationId = 1L;
        
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(dashboardService.getCandidateApplicationDetail(applicationId)).thenReturn(null);

            // Act & Assert
            assertThrows(AccessDeniedException.class, () -> {
                candidateController.getCandidateApplicationDetail(applicationId);
            });
            
            verify(dashboardService).getCandidateApplicationDetail(applicationId);
        }
    }

    // @Test
    // void testWithdrawApplication_Success() {
    //     // Arrange
    //     Long applicationId = 1L;
        
    //     try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
    //         securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
    //         doNothing().when(jobApplicationService).withdrawApplication(applicationId);

    //         // Act
    //         ResponseEntity<String> response = candidateController.withdrawApplication(applicationId);

    //         // Assert
    //         assertEquals(HttpStatus.OK, response.getStatusCode());
    //         assertEquals("Job Application withdrawn successfully.", response.getBody());
    //         verify(jobApplicationService).withdrawApplication(applicationId);
    //     }
    // }

    @Test
    void testWithdrawApplication_NotAuthenticated() {
        // Arrange
        Long applicationId = 1L;
        
        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(null);

            // Act & Assert
            assertThrows(AccessDeniedException.class, () -> {
                candidateController.withdrawApplication(applicationId);
            });
        }
    }
}
