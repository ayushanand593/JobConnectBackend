package com.dcode.jobconnect.dto;

import lombok.Data;

import java.util.List;

import com.dcode.jobconnect.enums.JobType;

@Data
public class JobSearchRequestDTO {
    private String companyName;
    private String jobTitle;
    private String location;
    private List<String> skills;
    private String experienceLevel;
    private JobType jobType;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;

    // Default values for pagination
    public Integer getPage() {
        return page == null ? 0 : page;
    }

    public Integer getSize() {
        return size == null ? 10 : size;
    }

    public String getSortBy() {
        return sortBy == null ? "createdAt" : sortBy;
    }

    public String getSortDirection() {
        return sortDirection == null ? "DESC" : sortDirection;
    }
}
