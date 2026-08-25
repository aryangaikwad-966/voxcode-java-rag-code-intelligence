CREATE TABLE evaluation_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    investigation_id BIGINT NOT NULL,
    execution_id BIGINT,
    rag_precision_score DECIMAL(5,2),
    rag_recall_score DECIMAL(5,2),
    rag_f1_score DECIMAL(5,2),
    finding_accuracy_score DECIMAL(5,2),
    remediation_success_rate DECIMAL(5,2),
    overall_score DECIMAL(5,2),
    evaluation_notes TEXT,
    evaluated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (investigation_id) REFERENCES investigations(id) ON DELETE CASCADE,
    FOREIGN KEY (execution_id) REFERENCES executions(id) ON DELETE SET NULL,
    INDEX idx_investigation_id (investigation_id),
    INDEX idx_execution_id (execution_id),
    INDEX idx_evaluated_at (evaluated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
