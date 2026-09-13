package com.workflow.dag_engine.interfaces.ai;

import com.workflow.dag_engine.models.graph.GraphUpdateRequest;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;

public interface AiGraphService {

    /**
     * Translates a natural language user description into a structured GraphUploadRequest.
     *
     * @param prompt User's natural language request to create or modify a DAG.
     * @param currentGraphJson The JSON representation of the current graph (if modifying), or null if creating a new one.
     * @return A populated GraphUpdateRequest ready for validation and persistence.
     * @throws Exception If the AI fails to generate a valid JSON structure.
     */
    GraphUpdateRequest generateGraph(String prompt, String currentGraphJson) throws Exception;

    /**
     * Self-correction method that attempts to fix a previously generated graph
     * based on validation error feedback.
     *
     * @param prompt The original user prompt.
     * @param currentGraphJson The original graph context if this was an update request.
     * @param invalidGraph The GraphUpdateRequest that failed validation.
     * @param errorMessage The specific validation error message (e.g. cycle detected).
     * @return A corrected GraphUpdateRequest.
     * @throws Exception If the AI fails to generate a valid JSON structure.
     */
    GraphUpdateRequest fixGraph(String prompt, String currentGraphJson, GraphUpdateRequest invalidGraph, String errorMessage) throws Exception;
}
