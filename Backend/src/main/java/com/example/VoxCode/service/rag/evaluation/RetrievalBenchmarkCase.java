package com.example.VoxCode.service.rag.evaluation;

import java.util.Set;

import com.example.VoxCode.dto.rag.RagQuery;

/**
 * A semantic retrieval query and the chunk IDs considered relevant for evaluation.
 */
public record RetrievalBenchmarkCase(
        String name,
        RagQuery query,
        Set<String> relevantChunkIds) {

    public RetrievalBenchmarkCase {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Benchmark case name must not be blank");
        }
        if (query == null) {
            throw new IllegalArgumentException("Benchmark case query must not be null");
        }
        if (relevantChunkIds == null || relevantChunkIds.isEmpty()) {
            throw new IllegalArgumentException("Benchmark case needs at least one relevant chunk ID");
        }
        relevantChunkIds = Set.copyOf(relevantChunkIds);
    }
}
