package com.example.VoxCode.repository;

import com.example.VoxCode.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    
    List<Plan> findByInvestigationId(Long investigationId);
    
    List<Plan> findByInvestigationIdAndStatus(Long investigationId, String status);
    
    List<Plan> findByStatus(String status);
}
