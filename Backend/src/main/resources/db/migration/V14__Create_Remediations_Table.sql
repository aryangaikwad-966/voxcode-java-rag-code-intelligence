-- VXC-200: Create remediations table for tracking remediation attempts and rollback state.
CREATE TABLE remediations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    approval_id BIGINT NOT NULL,
    transformation_strategy VARCHAR(50) NOT NULL,
    diff TEXT,
    workspace_path VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    within_scope BOOLEAN NOT NULL DEFAULT TRUE,
    error_message TEXT,
    modified_files TEXT,
    rollback_info TEXT,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE CASCADE,
    FOREIGN KEY (approval_id) REFERENCES approvals(id) ON DELETE CASCADE,
    INDEX idx_plan_id (plan_id),
    INDEX idx_approval_id (approval_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
