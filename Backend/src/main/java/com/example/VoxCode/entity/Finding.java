package com.example.VoxCode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "findings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Finding {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investigation_id", nullable = false)
    private Investigation investigation;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 50)
    private String severity = "MEDIUM";
    
    @Column(length = 100)
    private String category;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "affected_files", columnDefinition = "JSON")
    private String affectedFiles;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evidence_data", columnDefinition = "JSON")
    private String evidenceData;
    
    @Column(length = 50)
    private String status = "PENDING_VALIDATION";
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
