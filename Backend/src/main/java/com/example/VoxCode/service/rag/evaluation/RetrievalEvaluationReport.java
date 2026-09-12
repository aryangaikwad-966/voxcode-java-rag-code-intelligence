package com.example.VoxCode.service.rag.evaluation;

import java.util.List;

/**
 * Aggregate result for a retrieval benchmark run.
 */
public record RetrievalEvaluationReport(
        int k,
        List<RetrievalEvaluationMetrics> caseMetrics,
        double meanRecallAtK,
        double meanPrecisionAtK,
        double meanReciprocalRank,
        double meanNdcgAtK,
        double meanLatencyMillis) {

    public RetrievalEvaluationReport {
        caseMetrics = List.copyOf(caseMetrics);
    }
}
