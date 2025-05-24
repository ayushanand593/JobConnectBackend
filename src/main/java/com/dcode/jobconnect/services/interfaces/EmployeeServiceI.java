package com.dcode.jobconnect.services.interfaces;

import java.util.List;

import com.dcode.jobconnect.dto.EmployerProfileDTO;
import com.dcode.jobconnect.dto.EmployerProfileUpdateDTO;
import com.dcode.jobconnect.dto.JobDTO;

public interface EmployeeServiceI {

    EmployerProfileDTO getEmployerById(Long employerId);

    EmployerProfileDTO getCurrentEmployerProfile();

    EmployerProfileDTO updateProfile(EmployerProfileUpdateDTO dto);

    void deleteEmployerById(Long employerId);

    List<JobDTO> getJobsByEmployerId(Long employerId);

    

}
