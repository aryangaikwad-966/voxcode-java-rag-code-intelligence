package com.example.VoxCode.service.rag.evaluation;

/**
 * Quantitative metrics for one retrieval benchmark case.
 */
public record RetrievalEvaluationMetrics(
        String caseName,
        double recallAtK,
        double precisionAtK,
        double meanReciprocalRank,
        double ndcgAtK,
        double latencyMillis,
        int retrievedCount) {
}
