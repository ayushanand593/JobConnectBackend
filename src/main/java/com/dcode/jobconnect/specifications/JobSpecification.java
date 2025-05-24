package com.dcode.jobconnect.specifications;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.dcode.jobconnect.dto.JobSearchRequestDTO;
import com.dcode.jobconnect.entities.Job;
import com.dcode.jobconnect.entities.Skill;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

public class JobSpecification {

    public static Specification<Job> searchJobs(JobSearchRequestDTO searchRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by company name
            if (StringUtils.hasText(searchRequest.getCompanyName())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("company").get("companyName")),
                        "%" + searchRequest.getCompanyName().toLowerCase() + "%"
                ));
            }

            // Search by job title
            if (StringUtils.hasText(searchRequest.getJobTitle())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + searchRequest.getJobTitle().toLowerCase() + "%"
                ));
            }

            // Search by location
            if (StringUtils.hasText(searchRequest.getLocation())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("location")),
                        "%" + searchRequest.getLocation().toLowerCase() + "%"
                ));
            }

            // Search by experience level
            if (StringUtils.hasText(searchRequest.getExperienceLevel())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("experienceLevel")),
                        "%" + searchRequest.getExperienceLevel().toLowerCase() + "%"
                ));
            }

            // Search by job type
            if (searchRequest.getJobType() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("jobType"),
                        searchRequest.getJobType()
                ));
            }

            // Search by skills
            if (query!=null && searchRequest.getSkills() != null && !searchRequest.getSkills().isEmpty()) {
                // Need to use distinct to avoid duplicate results when searching by skills
                query.distinct(true);
                
                Join<Job, Skill> skillJoin = root.join("skills");
                List<Predicate> skillPredicates = new ArrayList<>();
                
                for (String skill : searchRequest.getSkills()) {
                    skillPredicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(skillJoin.get("name")),
                            "%" + skill.toLowerCase() + "%"
                    ));
                }
                
                // We want to match any of the skills provided
                predicates.add(criteriaBuilder.or(skillPredicates.toArray(new Predicate[0])));
            }

            // Only show open jobs by default
            predicates.add(criteriaBuilder.equal(root.get("status"), "OPEN"));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

