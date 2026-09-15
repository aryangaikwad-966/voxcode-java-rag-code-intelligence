package com.example.VoxCode.agent.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.VoxCode.agent.dto.InvestigationDecision;
import com.example.VoxCode.agent.dto.RequestClassification;
import com.example.VoxCode.agent.tools.AstTools;
import com.example.VoxCode.agent.tools.GraphTools;
import com.example.VoxCode.agent.tools.RagTools;
import com.example.VoxCode.agent.tools.RepositoryTools;
import com.example.VoxCode.entity.AgentTrace;
import com.example.VoxCode.entity.CodeRepository;
import com.example.VoxCode.entity.Investigation;
import com.example.VoxCode.repository.AgentTraceRepository;
import com.example.VoxCode.repository.CodeRepositoryRepository;
import com.example.VoxCode.repository.InvestigationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Core investigation agent that implements the Request → Hypothesis → Tool → Observation → Evidence → Decision → Finding loop.
 * Uses ONE primary Spring AI agent (not multi-agent swarm).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvestigationAgentService {

    private final ChatClient chatClient;
    private final RequestClassificationService classificationService;
    private final AstTools astTools;
    private final GraphTools graphTools;
    private final RagTools ragTools;
    private final RepositoryTools repositoryTools;
    private final InvestigationRepository investigationRepository;
    private final AgentTraceRepository agentTraceRepository;
    private final CodeRepositoryRepository codeRepositoryRepository;
    private final ObjectMapper objectMapper;

    /**
     * Orchestrates the complete investigation workflow.
     */
    @Transactional
    public Investigation orchestrateInvestigation(Long repositoryId, String userRequest) {
        log.info("Starting investigation for repository {} with request: {}", repositoryId, userRequest);
        
        // Step 1: Classify the request
        RequestClassification classification = classificationService.classifyRequest(userRequest);
        
        // Step 2: Reject OUT_OF_SCOPE requests immediately
        if (classification == RequestClassification.OUT_OF_SCOPE) {
            Investigation investigation = createInvestigation(repositoryId, userRequest, "REJECTED_OUT_OF_SCOPE");
            persistTrace(investigation, "CLASSIFICATION", "Request rejected as out of scope", 
                    Map.of("request", userRequest), Map.of("classification", classification.toString()));
            return investigation;
        }
        
        // Step 3: Create investigation record
        Investigation investigation = createInvestigation(repositoryId, userRequest, "IN_PROGRESS");
        persistTrace(investigation, "CLASSIFICATION", "Request classified", 
                Map.of("request", userRequest), Map.of("classification", classification.toString()));
        
        // Step 4: Run investigation loop
        InvestigationDecision finalDecision = runInvestigationLoop(investigation, userRequest, classification);
        
        // Step 5: Update investigation status based on outcome
        if (finalDecision.sufficientEvidence()) {
            investigation.setStatus("COMPLETED");
            investigation.setCompletedAt(LocalDateTime.now());
            persistTrace(investigation, "FINDING", "Investigation completed with finding", 
                    Map.of("finding", finalDecision.preliminaryFinding()), null);
        } else {
            investigation.setStatus("FAILED_INSUFFICIENT_EVIDENCE");
            investigation.setCompletedAt(LocalDateTime.now());
            persistTrace(investigation, "FAILURE", "Investigation failed due to insufficient evidence", null, null);
        }
        
        investigationRepository.save(investigation);
        log.info("Investigation {} completed with status: {}", investigation.getId(), investigation.getStatus());
        
        return investigation;
    }

    /**
     * Runs the investigation loop: Request → Hypothesis → Tool → Observation → Evidence → Decision → Finding
     */
    private InvestigationDecision runInvestigationLoop(Investigation investigation, String userRequest, RequestClassification classification) {
        int maxIterations = 10; // Prevent infinite loops
        InvestigationDecision currentDecision = null;
        
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            log.info("Investigation iteration {} for investigation {}", iteration + 1, investigation.getId());
            
            // Get next decision from agent
            currentDecision = getNextDecision(investigation, userRequest, classification, currentDecision);
            
            persistTrace(investigation, "DECISION", "Agent decision", 
                    Map.of("iteration", iteration), 
                    Map.of("action", currentDecision.action(), "reason", currentDecision.reason()));
            
            // Handle different decision actions
            switch (currentDecision.action()) {
                case CALL_TOOL:
                    String toolResult = executeTool(currentDecision);
                    persistTrace(investigation, "TOOL_CALL", "Tool execution", 
                            Map.of("tool", currentDecision.toolName(), "params", currentDecision.toolParameters()),
                            Map.of("result", toolResult));
                    break;
                    
                case MAKE_FINDING:
                    // Agent has sufficient evidence to make a finding
                    log.info("Agent decided to make finding: {}", currentDecision.preliminaryFinding());
                    return currentDecision;
                    
                case REQUEST_INFO:
                    // Agent needs more information from user
                    log.info("Agent requested additional information: {}", currentDecision.reason());
                    investigation.setStatus("AWAITING_USER_INPUT");
                    investigationRepository.save(investigation);
                    return currentDecision;
                    
                case STOP:
                    // Agent decided to stop without a finding
                    log.info("Agent decided to stop investigation: {}", currentDecision.reason());
                    return currentDecision;
            }
        }
        
        // Max iterations reached without finding
        log.warn("Investigation reached max iterations without finding");
        return new InvestigationDecision(
                InvestigationDecision.DecisionAction.STOP,
                null,
                null,
                "Maximum iterations reached without sufficient evidence",
                0.0,
                false,
                null
        );
    }

    /**
     * Gets the next decision from the LLM based on current context.
     */
    private InvestigationDecision getNextDecision(Investigation investigation, String userRequest, 
                                                  RequestClassification classification, InvestigationDecision previousDecision) {
        try {
            String prompt = buildInvestigationPrompt(investigation, userRequest, classification, previousDecision);
            
            String response = chatClient.prompt()
                    .user(prompt)
                    .system("""
                        You are an investigation agent for VoxCode, a Java/Spring repository intelligence system.
                        
                        Your goal is to investigate the user's request using available tools and make evidence-based decisions.
                        
                        Available tools:
                        - findClass(repositoryId, className): Find a class by name
                        - findMethod(repositoryId, methodName): Find methods by name
                        - findAnnotation(repositoryId, annotationName): Find classes with specific annotation
                        - findDependencies(repositoryId, classFqn): Find class dependencies
                        - findDependents(repositoryId, classFqn): Find classes that depend on this class
                        - searchSemanticContext(repositoryId, query): Search for semantic context
                        - readFile(repositoryId, filePath): Read a file from the repository
                        - listFiles(repositoryId, directoryPath): List files in a directory
                        
                        Output your decision as JSON with this structure:
                        {
                            "action": "CALL_TOOL" | "MAKE_FINDING" | "REQUEST_INFO" | "STOP",
                            "toolName": "tool name (if CALL_TOOL)",
                            "toolParameters": "parameters as JSON string (if CALL_TOOL)",
                            "reason": "explanation of your decision",
                            "confidence": 0.0 to 1.0,
                            "sufficientEvidence": true/false,
                            "preliminaryFinding": "finding description (if sufficientEvidence is true)"
                        }
                        
                        You MUST NOT make a finding without sufficient evidence from tools.
                        You MUST NOT expose hidden chain-of-thought in your output.
                        """)
                    .call()
                    .content()
                    .trim();
            
            // Parse JSON response
            return objectMapper.readValue(response, InvestigationDecision.class);
            
        } catch (Exception e) {
            log.error("Failed to get next decision from agent", e);
            return new InvestigationDecision(
                    InvestigationDecision.DecisionAction.STOP,
                    null,
                    null,
                    "Failed to get decision: " + e.getMessage(),
                    0.0,
                    false,
                    null
            );
        }
    }

    /**
     * Executes a tool call based on the agent's decision.
     * Simplified version for initial implementation.
     */
    private String executeTool(InvestigationDecision decision) {
        try {
            String toolName = decision.toolName();
            log.info("Executing tool: {}", toolName);
            
            // For now, return a placeholder response
            // Full implementation would parse parameters and call actual tools
            return "Tool " + toolName + " executed (placeholder implementation)";
            
        } catch (Exception e) {
            log.error("Failed to execute tool: {}", decision.toolName(), e);
            return "Error executing tool: " + e.getMessage();
        }
    }

    /**
     * Builds the investigation prompt for the agent.
     */
    private String buildInvestigationPrompt(Investigation investigation, String userRequest, 
                                          RequestClassification classification, InvestigationDecision previousDecision) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("User Request: ").append(userRequest).append("\n");
        prompt.append("Classification: ").append(classification).append("\n");
        prompt.append("Repository ID: ").append(investigation.getRepository().getId()).append("\n");
        
        if (previousDecision != null) {
            prompt.append("Previous Decision: ").append(previousDecision.action())
                   .append(" - ").append(previousDecision.reason()).append("\n");
        }
        
        prompt.append("\nDetermine your next action. Should you call a tool to gather more evidence, ");
        prompt.append("or do you have sufficient evidence to make a finding?");
        
        return prompt.toString();
    }

    /**
     * Creates a new investigation record.
     */
    private Investigation createInvestigation(Long repositoryId, String userRequest, String status) {
        Investigation investigation = new Investigation();
        // Load the repository from database
        CodeRepository repository = codeRepositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found: " + repositoryId));
        investigation.setRepository(repository);
        investigation.setTitle(userRequest.substring(0, Math.min(255, userRequest.length())));
        investigation.setDescription(userRequest);
        investigation.setStatus(status);
        investigation.setStartedAt(LocalDateTime.now());
        return investigationRepository.save(investigation);
    }

    /**
     * Persists an agent trace for observability.
     */
    private void persistTrace(Investigation investigation, String stepType, String stepDescription, 
                             Map<String, Object> inputData, Map<String, Object> outputData) {
        try {
            AgentTrace trace = new AgentTrace();
            trace.setInvestigation(investigation);
            trace.setStepType(stepType);
            trace.setStepDescription(stepDescription);
            trace.setInputData(inputData != null ? objectMapper.writeValueAsString(inputData) : null);
            trace.setOutputData(outputData != null ? objectMapper.writeValueAsString(outputData) : null);
            agentTraceRepository.save(trace);
        } catch (Exception e) {
            log.error("Failed to persist agent trace", e);
        }
    }
}