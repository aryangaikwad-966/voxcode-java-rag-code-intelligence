CREATE TABLE verification_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    execution_id BIGINT NOT NULL,
    build_status VARCHAR(50),
    test_status VARCHAR(50),
    static_analysis_status VARCHAR(50),
    diff_validation_status VARCHAR(50),
    total_files_modified INT DEFAULT 0,
    unauthorized_files_detected BOOLEAN DEFAULT FALSE,
    verification_log TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    verified_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (execution_id) REFERENCES executions(id) ON DELETE CASCADE,
    INDEX idx_execution_id (execution_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
