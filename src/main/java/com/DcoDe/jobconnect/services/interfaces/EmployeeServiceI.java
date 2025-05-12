package com.DcoDe.jobconnect.services.interfaces;

import java.util.List;

import com.DcoDe.jobconnect.dto.EmployerProfileDTO;
import com.DcoDe.jobconnect.dto.EmployerProfileUpdateDTO;
import com.DcoDe.jobconnect.dto.JobDTO;

public interface EmployeeServiceI {
    EmployerProfileDTO getCurrentEmployerProfile();

    EmployerProfileDTO updateProfile(EmployerProfileUpdateDTO dto);

    void deleteEmployerById(Long employerId);

    List<JobDTO> getJobsByEmployerId(Long employerId);

}
