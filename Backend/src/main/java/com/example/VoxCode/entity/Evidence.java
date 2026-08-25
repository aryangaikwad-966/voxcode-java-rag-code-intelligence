package com.example.VoxCode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "evidence")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evidence {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finding_id", nullable = false)
    private Finding finding;
    
    @Column(name = "evidence_type", nullable = false, length = 50)
    private String evidenceType;
    
    @Column(length = 100)
    private String source;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private String metadata;
    
    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
