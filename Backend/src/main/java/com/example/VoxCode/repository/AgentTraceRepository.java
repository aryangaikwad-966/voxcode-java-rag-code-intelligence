package com.example.VoxCode.repository;

import com.example.VoxCode.entity.AgentTrace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentTraceRepository extends JpaRepository<AgentTrace, Long> {
    
    List<AgentTrace> findByInvestigationIdOrderByTimestampAsc(Long investigationId);
    
    List<AgentTrace> findByInvestigationIdAndStepType(Long investigationId, String stepType);
}
