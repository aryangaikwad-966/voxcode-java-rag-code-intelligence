CREATE TABLE evidence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    finding_id BIGINT NOT NULL,
    evidence_type VARCHAR(50) NOT NULL,
    source VARCHAR(100),
    content TEXT NOT NULL,
    metadata JSON,
    confidence_score DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (finding_id) REFERENCES findings(id) ON DELETE CASCADE,
    INDEX idx_finding_id (finding_id),
    INDEX idx_evidence_type (evidence_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
