package com.example.VoxCode.repository;

import com.example.VoxCode.entity.Investigation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestigationRepository extends JpaRepository<Investigation, Long> {
    
    List<Investigation> findByRepositoryId(Long repositoryId);
    
    List<Investigation> findByRepositoryIdAndStatus(Long repositoryId, String status);
    
    List<Investigation> findByStatus(String status);
}
