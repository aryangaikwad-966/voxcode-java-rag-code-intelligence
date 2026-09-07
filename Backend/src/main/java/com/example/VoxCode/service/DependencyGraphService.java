package com.example.VoxCode.service;

import com.example.VoxCode.dto.ast.ClassInfo;
import com.example.VoxCode.dto.ast.FieldInfo;
import com.example.VoxCode.dto.ast.MethodInfo;
import com.example.VoxCode.dto.graph.DependencyEdge;
import com.example.VoxCode.dto.graph.DependencyNode;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Constructs and queries a dependency graph using JGraphT based on AST data.
 */
@Slf4j
@Service
public class DependencyGraphService {

    /**
     * Builds a dependency graph from a list of ClassInfo objects extracted from AST.
     *
     * @param classes The classes to include in the graph.
     * @return The constructed directed graph.
     */
    public Graph<DependencyNode, DependencyEdge> buildGraph(List<ClassInfo> classes) {
        Graph<DependencyNode, DependencyEdge> graph = new DirectedMultigraph<>(DependencyEdge.class);
        Map<String, DependencyNode> nodeLookup = new HashMap<>();

        // First pass: Create nodes for all classes, interfaces, enums and their methods
        for (ClassInfo classInfo : classes) {
            DependencyNode classNode = createClassNode(classInfo);
            graph.addVertex(classNode);
            nodeLookup.put(classNode.getId(), classNode);

            for (MethodInfo methodInfo : classInfo.getMethods()) {
                DependencyNode methodNode = createMethodNode(classInfo, methodInfo);
                graph.addVertex(methodNode);
                nodeLookup.put(methodNode.getId(), methodNode);

                // Add CONTAINS edge
                graph.addEdge(classNode, methodNode, new DependencyEdge(DependencyEdge.EdgeType.CONTAINS));
            }
        }

        // Second pass: Create edges based on dependencies
        for (ClassInfo classInfo : classes) {
            DependencyNode sourceNode = nodeLookup.get(classInfo.getFullyQualifiedName());
            if (sourceNode == null) continue;

            // 1. EXTENDS
            if (classInfo.getSuperClass() != null) {
                String superClassName = resolveTypeName(classInfo.getSuperClass(), classInfo.getImports(), classInfo.getPackageName());
                DependencyNode targetNode = nodeLookup.get(superClassName);
                if (targetNode != null) {
                    graph.addEdge(sourceNode, targetNode, new DependencyEdge(DependencyEdge.EdgeType.EXTENDS));
                }
            }

            // 2. IMPLEMENTS
            if (classInfo.getImplementedInterfaces() != null) {
                for (String interfaceName : classInfo.getImplementedInterfaces()) {
                    String resolvedName = resolveTypeName(interfaceName, classInfo.getImports(), classInfo.getPackageName());
                    DependencyNode targetNode = nodeLookup.get(resolvedName);
                    if (targetNode != null) {
                        graph.addEdge(sourceNode, targetNode, new DependencyEdge(DependencyEdge.EdgeType.IMPLEMENTS));
                    }
                }
            }

            // 3. DEPENDS_ON (Field types)
            if (classInfo.getFields() != null) {
                for (FieldInfo field : classInfo.getFields()) {
                    String resolvedName = resolveTypeName(field.getType(), classInfo.getImports(), classInfo.getPackageName());
                    DependencyNode targetNode = nodeLookup.get(resolvedName);
                    if (targetNode != null && !targetNode.getId().equals(sourceNode.getId())) {
                        graph.addEdge(sourceNode, targetNode, new DependencyEdge(DependencyEdge.EdgeType.DEPENDS_ON));
                    }
                }
            }

            // 4. DEPENDS_ON (Method return types and parameters) & CALLS (Method calls)
            for (MethodInfo methodInfo : classInfo.getMethods()) {
                DependencyNode methodNode = nodeLookup.get(getMethodId(classInfo, methodInfo));

                // Add DEPENDS_ON for method return type to the class node
                String returnType = resolveTypeName(methodInfo.getReturnType(), classInfo.getImports(), classInfo.getPackageName());
                DependencyNode returnTargetNode = nodeLookup.get(returnType);
                if (returnTargetNode != null && !returnTargetNode.getId().equals(sourceNode.getId())) {
                     graph.addEdge(sourceNode, returnTargetNode, new DependencyEdge(DependencyEdge.EdgeType.DEPENDS_ON));
                }
                
                // Add DEPENDS_ON for method parameters
                for (String paramType : methodInfo.getParameterTypes()) {
                     String resolvedParamType = resolveTypeName(paramType, classInfo.getImports(), classInfo.getPackageName());
                     DependencyNode paramTargetNode = nodeLookup.get(resolvedParamType);
                     if (paramTargetNode != null && !paramTargetNode.getId().equals(sourceNode.getId())) {
                         graph.addEdge(sourceNode, paramTargetNode, new DependencyEdge(DependencyEdge.EdgeType.DEPENDS_ON));
                     }
                }

                // Add CALLS edges (best effort: we only have method names, not signatures of calls)
                // We link to any method with a matching name in the graph that we can find
                if (methodInfo.getMethodCalls() != null && methodNode != null) {
                    for (String calledMethodName : methodInfo.getMethodCalls()) {
                        // Find any method node in the lookup with this name (naive resolution for now)
                        for (DependencyNode possibleTarget : nodeLookup.values()) {
                            if (possibleTarget.getType() == DependencyNode.NodeType.METHOD
                                    && possibleTarget.getName().equals(calledMethodName)
                                    && !possibleTarget.getId().equals(methodNode.getId())) {
                                graph.addEdge(methodNode, possibleTarget, new DependencyEdge(DependencyEdge.EdgeType.CALLS));
                            }
                        }
                    }
                }
            }
        }

        log.info("Built dependency graph with {} nodes and {} edges", graph.vertexSet().size(), graph.edgeSet().size());
        return graph;
    }

    /**
     * Gets all nodes that the specified class depends on directly.
     */
    public Set<DependencyNode> getDependencies(Graph<DependencyNode, DependencyEdge> graph, String classFqn) {
        DependencyNode node = findNodeById(graph, classFqn);
        if (node == null) return Set.of();
        return graph.outgoingEdgesOf(node).stream()
                .filter(e -> e.getType() == DependencyEdge.EdgeType.DEPENDS_ON || e.getType() == DependencyEdge.EdgeType.EXTENDS || e.getType() == DependencyEdge.EdgeType.IMPLEMENTS)
                .map(graph::getEdgeTarget)
                .collect(Collectors.toSet());
    }

    /**
     * Gets all nodes that depend on the specified class directly (impact analysis).
     */
    public Set<DependencyNode> getDependents(Graph<DependencyNode, DependencyEdge> graph, String classFqn) {
        DependencyNode node = findNodeById(graph, classFqn);
        if (node == null) return Set.of();
        return graph.incomingEdgesOf(node).stream()
                .filter(e -> e.getType() == DependencyEdge.EdgeType.DEPENDS_ON || e.getType() == DependencyEdge.EdgeType.EXTENDS || e.getType() == DependencyEdge.EdgeType.IMPLEMENTS)
                .map(graph::getEdgeSource)
                .collect(Collectors.toSet());
    }

    private DependencyNode findNodeById(Graph<DependencyNode, DependencyEdge> graph, String id) {
        return graph.vertexSet().stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private DependencyNode createClassNode(ClassInfo classInfo) {
        DependencyNode.NodeType type = DependencyNode.NodeType.CLASS;
        if ("INTERFACE".equals(classInfo.getClassType())) type = DependencyNode.NodeType.INTERFACE;
        if ("ENUM".equals(classInfo.getClassType())) type = DependencyNode.NodeType.ENUM;

        return DependencyNode.builder()
                .id(classInfo.getFullyQualifiedName())
                .name(classInfo.getClassName())
                .type(type)
                .build();
    }

    private DependencyNode createMethodNode(ClassInfo classInfo, MethodInfo methodInfo) {
        return DependencyNode.builder()
                .id(getMethodId(classInfo, methodInfo))
                .name(methodInfo.getName())
                .type(DependencyNode.NodeType.METHOD)
                .build();
    }

    private String getMethodId(ClassInfo classInfo, MethodInfo methodInfo) {
        return classInfo.getFullyQualifiedName() + "." + methodInfo.getName();
    }

    /**
     * Attempts to resolve a simple type name to a fully qualified name using imports.
     */
    private String resolveTypeName(String typeName, List<String> imports, String currentPackage) {
        if (typeName == null) return null;
        // Strip generics if any
        if (typeName.contains("<")) {
            typeName = typeName.substring(0, typeName.indexOf("<"));
        }
        
        for (String imp : imports) {
            if (imp.endsWith("." + typeName)) {
                return imp;
            }
        }
        // If not found in imports, assume it's in the same package (naive, but works for simple cases)
        if (!currentPackage.isEmpty() && !typeName.contains(".")) {
             return currentPackage + "." + typeName;
        }
        return typeName;
    }
}
