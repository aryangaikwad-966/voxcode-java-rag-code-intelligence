CREATE TABLE findings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    investigation_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(50) DEFAULT 'MEDIUM',
    category VARCHAR(100),
    affected_files JSON,
    evidence_data JSON,
    status VARCHAR(50) DEFAULT 'PENDING_VALIDATION',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (investigation_id) REFERENCES investigations(id) ON DELETE CASCADE,
    INDEX idx_investigation_id (investigation_id),
    INDEX idx_status (status),
    INDEX idx_severity (severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
