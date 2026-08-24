package com.example.VoxCode.repository;

import com.example.VoxCode.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    
    List<Approval> findByPlanId(Long planId);
    
    List<Approval> findByUserId(Long userId);
    
    Optional<Approval> findByPlanIdAndUserId(Long planId, Long userId);
    
    List<Approval> findByDecision(String decision);
}
