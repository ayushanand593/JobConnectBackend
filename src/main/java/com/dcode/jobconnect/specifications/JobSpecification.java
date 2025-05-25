package com.dcode.jobconnect.specifications;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.dcode.jobconnect.dto.JobSearchRequestDTO;
import com.dcode.jobconnect.entities.Job;
import com.dcode.jobconnect.entities.Skill;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class JobSpecification {

    private JobSpecification() {
    }

    public static Specification<Job> searchJobs(JobSearchRequestDTO searchRequest) {
    return (root, query, criteriaBuilder) -> {
        List<Predicate> predicates = new ArrayList<>();

        addTextSearchPredicate(predicates, criteriaBuilder, root.get("company").get("companyName"), 
                             searchRequest.getCompanyName());
        addTextSearchPredicate(predicates, criteriaBuilder, root.get("title"), 
                             searchRequest.getJobTitle());
        addTextSearchPredicate(predicates, criteriaBuilder, root.get("location"), 
                             searchRequest.getLocation());
        addTextSearchPredicate(predicates, criteriaBuilder, root.get("experienceLevel"), 
                             searchRequest.getExperienceLevel());

        // Search by job type
        if (searchRequest.getJobType() != null) {
            predicates.add(criteriaBuilder.equal(root.get("jobType"), searchRequest.getJobType()));
        }

        // Search by skills
        addSkillsSearchPredicate(predicates, criteriaBuilder, root, query, searchRequest.getSkills());

        // Only show open jobs by default
        predicates.add(criteriaBuilder.equal(root.get("status"), "OPEN"));

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
}

private static void addTextSearchPredicate(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
                                         Path<String> field, String searchValue) {
    if (StringUtils.hasText(searchValue)) {
        predicates.add(criteriaBuilder.like(
                criteriaBuilder.lower(field),
                "%" + searchValue.toLowerCase() + "%"
        ));
    }
}

private static void addSkillsSearchPredicate(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
                                           Root<Job> root, CriteriaQuery<?> query, List<String> skills) {
    if (query == null || skills == null || skills.isEmpty()) {
        return;
    }

    query.distinct(true);
    Join<Job, Skill> skillJoin = root.join("skills");
    List<Predicate> skillPredicates = new ArrayList<>();
    
    for (String skill : skills) {
        skillPredicates.add(criteriaBuilder.like(
                criteriaBuilder.lower(skillJoin.get("name")),
                "%" + skill.toLowerCase() + "%"
        ));
    }
    
    predicates.add(criteriaBuilder.or(skillPredicates.toArray(new Predicate[0])));
}
}

