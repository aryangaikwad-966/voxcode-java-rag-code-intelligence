package com.example.VoxCode.evidence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.VoxCode.dto.ast.ClassInfo;
import com.example.VoxCode.dto.ast.MethodInfo;
import com.example.VoxCode.dto.index.GraphQueryResult;
import com.example.VoxCode.dto.rag.CodeChunk;
import com.example.VoxCode.entity.CodeRepository;
import com.example.VoxCode.entity.Evidence;
import com.example.VoxCode.entity.Finding;
import com.example.VoxCode.entity.Investigation;
import com.example.VoxCode.evidence.model.EvidenceItem;
import com.example.VoxCode.evidence.model.LineRange;
import com.example.VoxCode.evidence.model.StructuredFinding;
import com.example.VoxCode.evidence.service.EvidenceEngine;
import com.example.VoxCode.evidence.service.FindingSchemaValidator;
import com.example.VoxCode.repository.EvidenceRepository;
import com.example.VoxCode.repository.FindingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class EvidenceEngineTest {

    @Mock
    private FindingRepository findingRepository;

    @Mock
    private EvidenceRepository evidenceRepository;

    private ObjectMapper objectMapper;
    private FindingSchemaValidator schemaValidator;
    private EvidenceEngine evidenceEngine;

    private Investigation sampleInvestigation;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        schemaValidator = new FindingSchemaValidator(objectMapper);
        evidenceEngine = new EvidenceEngine(findingRepository, evidenceRepository, schemaValidator, objectMapper);

        CodeRepository repo = new CodeRepository();
        repo.setId(42L);

        sampleInvestigation = new Investigation();
        sampleInvestigation.setId(100L);
        sampleInvestigation.setRepository(repo);
    }

    @Test
    void recordFinding_persistsFindingAndEvidence_withValidJson() {
        // Arrange
        EvidenceItem item1 = EvidenceItem.astNode("AstTools.findClass", "public class SecurityService", "CLASS", LineRange.of(1, 50), Map.of());
        EvidenceItem item2 = EvidenceItem.graphEdge("GraphTools.findDependencies", "SecurityService -> AuthService", "DEPENDS_ON", "SecurityService", "AuthService");

        StructuredFinding structuredFinding = StructuredFinding.builder()
                .repositoryId(42L)
                .filePath("src/main/java/SecurityService.java")
                .lineRange(LineRange.of(10, 30))
                .targetClass("com.example.SecurityService")
                .targetMethod("checkAccess")
                .issueType("SECURITY_FLAW")
                .severity("CRITICAL")
                .title("Flawed Access Control")
                .description("checkAccess method always returns true")
                .evidenceReferences(List.of(item1, item2))
                .evidenceSource("AST")
                .validationStatus("PENDING_VALIDATION")
                .build();

        Finding savedFindingMock = new Finding();
        savedFindingMock.setId(555L);
        when(findingRepository.save(any(Finding.class))).thenReturn(savedFindingMock);

        // Act
        Finding result = evidenceEngine.recordFinding(sampleInvestigation, structuredFinding);

        // Assert
        assertNotNull(result);
        assertEquals(555L, result.getId());

        ArgumentCaptor<Finding> findingCaptor = ArgumentCaptor.forClass(Finding.class);
        verify(findingRepository).save(findingCaptor.capture());
        Finding capturedFinding = findingCaptor.getValue();

        assertEquals("Flawed Access Control", capturedFinding.getTitle());
        assertEquals("CRITICAL", capturedFinding.getSeverity());
        assertEquals("SECURITY_FLAW", capturedFinding.getCategory());
        assertEquals("PENDING_VALIDATION", capturedFinding.getStatus());
        assertTrue(capturedFinding.getAffectedFiles().contains("src/main/java/SecurityService.java"));
        assertTrue(capturedFinding.getEvidenceData().contains("com.example.SecurityService"));

        // Verify evidence saved
        ArgumentCaptor<Evidence> evidenceCaptor = ArgumentCaptor.forClass(Evidence.class);
        verify(evidenceRepository, times(2)).save(evidenceCaptor.capture());
        List<Evidence> savedEvidences = evidenceCaptor.getAllValues();
        assertEquals(2, savedEvidences.size());
        assertEquals("AST_NODE", savedEvidences.get(0).getEvidenceType());
        assertEquals("GRAPH_EDGE", savedEvidences.get(1).getEvidenceType());
    }

    @Test
    void recordFinding_invalidSchema_throwsException() {
        StructuredFinding invalidFinding = StructuredFinding.builder()
                .repositoryId(null) // missing repository ID
                .filePath("Test.java")
                .lineRange(LineRange.of(1, 5))
                .targetClass("Test")
                .issueType("BUG")
                .build();

        assertThrows(IllegalArgumentException.class, () ->
                evidenceEngine.recordFinding(sampleInvestigation, invalidFinding));
        verify(findingRepository, never()).save(any());
        verify(evidenceRepository, never()).save(any());
    }

    @Test
    void createAstClassEvidence_populatesMetadataProperly() {
        ClassInfo classInfo = ClassInfo.builder()
                .fullyQualifiedName("com.example.TestClass")
                .className("TestClass")
                .filePath("src/main/java/TestClass.java")
                .startLine(15)
                .endLine(80)
                .annotations(List.of("@Service"))
                .build();

        EvidenceItem evidence = evidenceEngine.createAstClassEvidence("AstTools.findClass", classInfo, "class content");

        assertEquals("AST_NODE", evidence.evidenceType());
        assertEquals("AstTools.findClass", evidence.source());
        assertEquals("class content", evidence.content());
        assertEquals(1.0, evidence.confidenceScore());
        assertEquals("CLASS", evidence.metadata().get("nodeType"));
        assertEquals(15, evidence.metadata().get("startLine"));
        assertEquals(80, evidence.metadata().get("endLine"));
        assertEquals("com.example.TestClass", evidence.metadata().get("fullyQualifiedName"));
        assertEquals("TestClass", evidence.metadata().get("className"));
    }

    @Test
    void createAstMethodEvidence_populatesMetadataProperly() {
        MethodInfo methodInfo = MethodInfo.builder()
                .name("findUser")
                .returnType("User")
                .startLine(25)
                .endLine(35)
                .annotations(List.of("@Transactional"))
                .build();

        EvidenceItem evidence = evidenceEngine.createAstMethodEvidence("AstTools.findMethod", methodInfo, "method snippet");

        assertEquals("AST_NODE", evidence.evidenceType());
        assertEquals("METHOD", evidence.metadata().get("nodeType"));
        assertEquals(25, evidence.metadata().get("startLine"));
        assertEquals(35, evidence.metadata().get("endLine"));
        assertEquals("findUser", evidence.metadata().get("methodName"));
    }

    @Test
    void createGraphEvidence_populatesMetadataProperly() {
        GraphQueryResult graphResult = GraphQueryResult.builder()
                .nodes(List.of())
                .relationships(List.of())
                .build();
        EvidenceItem evidence = evidenceEngine.createGraphEvidence("GraphTools.findDependencies", graphResult, "A", "B");

        assertEquals("GRAPH_EDGE", evidence.evidenceType());
        assertEquals("GraphTools.findDependencies", evidence.source());
        assertEquals("DEPENDS_ON", evidence.metadata().get("edgeType"));
        assertEquals("A", evidence.metadata().get("from"));
        assertEquals("B", evidence.metadata().get("to"));
    }

    @Test
    void createRagEvidence_populatesMetadataProperly() {
        CodeChunk chunk = CodeChunk.builder()
                .id("chunk-1")
                .content("void execute() { auth(); }")
                .filePath("src/Service.java")
                .startLine(10)
                .endLine(20)
                .build();

        EvidenceItem evidence = evidenceEngine.createRagEvidence("RagTools.searchSemanticContext", chunk, 0.88);

        assertEquals("RAG_CHUNK", evidence.evidenceType());
        assertEquals("src/Service.java", evidence.metadata().get("filePath"));
        assertEquals(10, evidence.metadata().get("startLine"));
        assertEquals(20, evidence.metadata().get("endLine"));
        assertEquals(0.88, evidence.confidenceScore());
    }

    @Test
    void getStructuredFinding_parsesStoredJson() {
        Finding finding = new Finding();
        finding.setId(77L);
        finding.setEvidenceData("""
                {
                    "repositoryId": 42,
                    "filePath": "src/App.java",
                    "lineRange": { "startLine": 5, "endLine": 12 },
                    "targetClass": "App",
                    "targetMethod": "main",
                    "issueType": "RESOURCE_LEAK",
                    "severity": "MEDIUM",
                    "title": "Stream unclosed",
                    "description": "FileInputStream is not closed",
                    "evidenceReferences": [],
                    "evidenceSource": "AST",
                    "validationStatus": "PENDING_VALIDATION"
                }
                """);

        when(findingRepository.findById(77L)).thenReturn(Optional.of(finding));

        Optional<StructuredFinding> result = evidenceEngine.getStructuredFinding(77L);
        assertTrue(result.isPresent());
        assertEquals(42L, result.get().repositoryId());
        assertEquals("src/App.java", result.get().filePath());
        assertEquals(5, result.get().lineRange().startLine());
        assertEquals(12, result.get().lineRange().endLine());
        assertEquals("RESOURCE_LEAK", result.get().issueType());
    }
}
