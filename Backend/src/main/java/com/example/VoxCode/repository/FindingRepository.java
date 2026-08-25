package com.example.VoxCode.repository;

import com.example.VoxCode.entity.Finding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FindingRepository extends JpaRepository<Finding, Long> {
    
    List<Finding> findByInvestigationId(Long investigationId);
    
    List<Finding> findByInvestigationIdAndStatus(Long investigationId, String status);
    
    List<Finding> findBySeverity(String severity);
    
    List<Finding> findByCategory(String category);
}
