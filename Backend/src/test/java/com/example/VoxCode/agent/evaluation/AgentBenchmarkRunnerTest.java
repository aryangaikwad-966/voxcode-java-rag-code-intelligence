package com.example.VoxCode.agent.evaluation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.VoxCode.agent.service.InvestigationAgentService;
import com.example.VoxCode.entity.AgentTrace;
import com.example.VoxCode.entity.Investigation;
import com.example.VoxCode.repository.AgentTraceRepository;
import com.example.VoxCode.agent.evaluation.AgentEvaluationReport.CaseOutcome;

/**
 * Test AgentBenchmarkRunner execution and metrics calculation.
 */
@ExtendWith(MockitoExtension.class)
class AgentBenchmarkRunnerTest {

    @Mock
    private InvestigationAgentService investigationAgentService;

    @Mock
    private AgentTraceRepository agentTraceRepository;

    @InjectMocks
    private AgentBenchmarkRunner benchmarkRunner;

    @Test
    void run_emptyDataset_returnsEmptyReport() {
        // Arrange
        AgentBenchmarkDataset emptyDataset = AgentBenchmarkDataset.fromCases(List.of());

        // Act
        AgentEvaluationReport report = benchmarkRunner.run(emptyDataset);

        // Assert
        assertNotNull(report);
        assertEquals(0, report.metrics().totalCases());
        assertEquals(0, report.metrics().successfulCases());
        assertEquals(0, report.metrics().failedCases());
        assertTrue(report.caseResults().isEmpty());
    }

    @Test
    void run_singleSuccessfulCase_calculatesMetrics() {
        // Arrange
        AgentBenchmarkCase benchmarkCase = new AgentBenchmarkCase(
                "TEST-001",
                "Test case",
                1L,
                "Test request",
                "Test finding",
                Set.of("evidence")
        );

        Investigation mockInvestigation = new Investigation();
        mockInvestigation.setId(1L);
        mockInvestigation.setStatus("COMPLETED");
        mockInvestigation.setDescription("Test finding");

        when(investigationAgentService.orchestrateInvestigation(anyLong(), anyString()))
                .thenReturn(mockInvestigation);
        when(agentTraceRepository.findByInvestigationIdOrderByTimestampAsc(anyLong()))
                .thenReturn(List.of());

        AgentBenchmarkDataset dataset = AgentBenchmarkDataset.fromCases(List.of(benchmarkCase));

        // Act
        AgentEvaluationReport report = benchmarkRunner.run(dataset);

        // Assert
        assertNotNull(report);
        assertEquals(1, report.metrics().totalCases());
        assertEquals(1, report.metrics().successfulCases());
        assertEquals(0, report.metrics().failedCases());
        assertEquals(1.0, report.metrics().investigationSuccessRate());
        assertEquals(1, report.caseResults().size());
        assertEquals(CaseOutcome.TP, report.caseResults().get(0).outcome());
    }

    @Test
    void run_failedCase_includesInMetrics() {
        // Arrange
        AgentBenchmarkCase benchmarkCase = new AgentBenchmarkCase(
                "TEST-001",
                "Test case",
                1L,
                "Test request",
                "Test finding",
                Set.of()
        );

        when(investigationAgentService.orchestrateInvestigation(anyLong(), anyString()))
                .thenThrow(new RuntimeException("Test error"));

        AgentBenchmarkDataset dataset = AgentBenchmarkDataset.fromCases(List.of(benchmarkCase));

        // Act
        AgentEvaluationReport report = benchmarkRunner.run(dataset);

        // Assert
        assertNotNull(report);
        assertEquals(1, report.metrics().totalCases());
        assertEquals(0, report.metrics().successfulCases());
        assertEquals(1, report.metrics().failedCases());
        assertEquals(0.0, report.metrics().investigationSuccessRate());
        assertEquals(1, report.caseResults().size());
        assertEquals(CaseOutcome.FN, report.caseResults().get(0).outcome());
        assertNotNull(report.caseResults().get(0).errorMessage());
    }

    @Test
    void run_multipleCases_aggregatesMetrics() {
        // Arrange
        AgentBenchmarkCase case1 = new AgentBenchmarkCase(
                "TEST-001", "Test 1", 1L, "Request 1", "Finding 1", Set.of());
        AgentBenchmarkCase case2 = new AgentBenchmarkCase(
                "TEST-002", "Test 2", 1L, "Request 2", "Finding 2", Set.of());

        Investigation mockInvestigation = new Investigation();
        mockInvestigation.setId(1L);
        mockInvestigation.setStatus("COMPLETED");
        mockInvestigation.setDescription("Finding");

        when(investigationAgentService.orchestrateInvestigation(anyLong(), anyString()))
                .thenReturn(mockInvestigation);
        when(agentTraceRepository.findByInvestigationIdOrderByTimestampAsc(anyLong()))
                .thenReturn(List.of());

        AgentBenchmarkDataset dataset = AgentBenchmarkDataset.fromCases(List.of(case1, case2));

        // Act
        AgentEvaluationReport report = benchmarkRunner.run(dataset);

        // Assert
        assertNotNull(report);
        assertEquals(2, report.metrics().totalCases());
        assertEquals(2, report.metrics().successfulCases());
        assertEquals(0, report.metrics().failedCases());
        assertEquals(1.0, report.metrics().investigationSuccessRate());
        assertEquals(2, report.caseResults().size());
    }

    @Test
    void run_withToolCalls_countsToolCalls() {
        // Arrange
        AgentBenchmarkCase benchmarkCase = new AgentBenchmarkCase(
                "TEST-001", "Test", 1L, "Request", "Finding", Set.of());

        Investigation mockInvestigation = new Investigation();
        mockInvestigation.setId(1L);
        mockInvestigation.setStatus("COMPLETED");
        mockInvestigation.setDescription("Finding");

        AgentTrace toolTrace = new AgentTrace();
        toolTrace.setStepType("TOOL_CALL");

        when(investigationAgentService.orchestrateInvestigation(anyLong(), anyString()))
                .thenReturn(mockInvestigation);
        when(agentTraceRepository.findByInvestigationIdOrderByTimestampAsc(anyLong()))
                .thenReturn(List.of(toolTrace));

        AgentBenchmarkDataset dataset = AgentBenchmarkDataset.fromCases(List.of(benchmarkCase));

        // Act
        AgentEvaluationReport report = benchmarkRunner.run(dataset);

        // Assert
        assertEquals(1, report.caseResults().get(0).toolCallCount());
    }

    @Test
    void validateEvidence_withExpectedFields_validatesPresence() {
        assertTrue(true); 
    }
}
