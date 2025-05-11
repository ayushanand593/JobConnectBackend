package com.DcoDe.jobconnect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.DcoDe.jobconnect.dto.CompanyDetailDTO;
import com.DcoDe.jobconnect.dto.CompanyRegistrationDTO;
import com.DcoDe.jobconnect.services.interfaces.CompanyServiceI;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

     private final CompanyServiceI companyService;

    @PostMapping("/register")
    public ResponseEntity<CompanyDetailDTO> registerCompany(@Valid @RequestBody CompanyRegistrationDTO dto) {
        CompanyDetailDTO registeredCompany = companyService.registerCompany(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredCompany);
    }

    @GetMapping("/{companyUniqueId}")
    public ResponseEntity<CompanyDetailDTO> getCompanyProfile(@PathVariable String companyUniqueId) {
        return ResponseEntity.ok(companyService.getCompanyByUniqueId(companyUniqueId));
    }

}
