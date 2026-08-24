package com.example.VoxCode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    private Execution execution;
    
    @Column(name = "build_status", length = 50)
    private String buildStatus;
    
    @Column(name = "test_status", length = 50)
    private String testStatus;
    
    @Column(name = "static_analysis_status", length = 50)
    private String staticAnalysisStatus;
    
    @Column(name = "diff_validation_status", length = 50)
    private String diffValidationStatus;
    
    @Column(name = "total_files_modified")
    private Integer totalFilesModified = 0;
    
    @Column(name = "unauthorized_files_detected")
    private Boolean unauthorizedFilesDetected = false;
    
    @Column(name = "verification_log", columnDefinition = "TEXT")
    private String verificationLog;
    
    @Column(length = 50)
    private String status = "PENDING";
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
