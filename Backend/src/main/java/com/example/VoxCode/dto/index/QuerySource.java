package com.example.VoxCode.dto.index;

/**
 * Identifies whether a repository index query is answered by
 * structural (AST) analysis or graph (dependency) traversal.
 */
public enum QuerySource {
    STRUCTURAL,
    GRAPH
}
