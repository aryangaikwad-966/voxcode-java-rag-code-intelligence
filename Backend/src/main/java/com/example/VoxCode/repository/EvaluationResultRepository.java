package com.example.VoxCode.repository;

import com.example.VoxCode.entity.EvaluationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationResultRepository extends JpaRepository<EvaluationResult, Long> {
    
    List<EvaluationResult> findByInvestigationId(Long investigationId);
    
    List<EvaluationResult> findByExecutionId(Long executionId);
}
