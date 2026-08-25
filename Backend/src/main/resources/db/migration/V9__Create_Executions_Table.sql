CREATE TABLE executions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'IN_PROGRESS',
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    docker_container_id VARCHAR(255),
    execution_log TEXT,
    FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE CASCADE,
    INDEX idx_plan_id (plan_id),
    INDEX idx_status (status),
    INDEX idx_started_at (started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
