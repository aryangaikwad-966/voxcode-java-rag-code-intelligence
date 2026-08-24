package com.example.VoxCode.repository;

import com.example.VoxCode.entity.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenceRepository extends JpaRepository<Evidence, Long> {
    
    List<Evidence> findByFindingId(Long findingId);
    
    List<Evidence> findByFindingIdAndEvidenceType(Long findingId, String evidenceType);
}
