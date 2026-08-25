CREATE TABLE agent_traces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    investigation_id BIGINT NOT NULL,
    step_type VARCHAR(50) NOT NULL,
    step_description TEXT,
    input_data JSON,
    output_data JSON,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (investigation_id) REFERENCES investigations(id) ON DELETE CASCADE,
    INDEX idx_investigation_id (investigation_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_step_type (step_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
