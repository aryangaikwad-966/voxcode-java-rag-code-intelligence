package com.example.VoxCode.repository;

import com.example.VoxCode.entity.VerificationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationResultRepository extends JpaRepository<VerificationResult, Long> {
    
    Optional<VerificationResult> findByExecutionId(Long executionId);
}
