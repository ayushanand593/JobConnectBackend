package com.DcoDe.jobconnect.services.interfaces;

import com.DcoDe.jobconnect.dto.EmployerProfileDTO;
import com.DcoDe.jobconnect.dto.EmployerProfileUpdateDTO;

public interface EmployeeServiceI {
    EmployerProfileDTO getCurrentEmployerProfile();

    EmployerProfileDTO updateProfile(EmployerProfileUpdateDTO dto);
}
