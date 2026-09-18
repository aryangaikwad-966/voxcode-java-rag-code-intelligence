package com.example.VoxCode.agent.evaluation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AgentEvaluationMetricsTest {

    @Test
    void constructor_validArgs_createsMetrics() {
        AgentEvaluationMetrics metrics = new AgentEvaluationMetrics(
                10, 8, 2, 0.8, 3.5, 2.1, 0.85, 7, 0.7, 0.8, 0.8, 0.8, 10000L, 1000.0);
        
        assertEquals(10, metrics.totalCases());
        assertEquals(0.8, metrics.investigationSuccessRate());
        assertEquals(0.8, metrics.precision());
    }

    @Test
    void constructor_invalidArgs_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new AgentEvaluationMetrics(-1, 0, 0, 0.0, 0.0, 0.0, 0.0, 0, 0.0, 0.0, 0.0, 0.0, 0L, 0.0));
    }

    @Test
    void empty_returnsEmptyMetrics() {
        AgentEvaluationMetrics metrics = AgentEvaluationMetrics.empty();
        assertEquals(0, metrics.totalCases());
        assertEquals(0.0, metrics.precision());
    }

    @Test
    void toMarkdown_formatsCorrectly() {
        AgentEvaluationMetrics metrics = new AgentEvaluationMetrics(
                10, 8, 2, 0.8, 3.5, 2.1, 0.85, 7, 0.7, 0.8, 0.8, 0.8, 10000L, 1000.0);
        String markdown = metrics.toMarkdown();
        
        assertTrue(markdown.contains("# Agent Evaluation Metrics"));
        assertTrue(markdown.contains("Success Rate: 80.00%"));
        assertTrue(markdown.contains("Precision: 0.80"));
    }
}
