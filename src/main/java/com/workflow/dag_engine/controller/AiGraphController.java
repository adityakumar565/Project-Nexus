package com.workflow.dag_engine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.workflow.dag_engine.interfaces.ai.AiGraphService;
import com.workflow.dag_engine.interfaces.wrapper.GraphInterface;
import com.workflow.dag_engine.models.graph.AiGraphRequest;
import com.workflow.dag_engine.models.graph.GraphResponse;
import com.workflow.dag_engine.models.graph.GraphUpdateRequest;
import com.workflow.dag_engine.models.validation.ErrorDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/workflow-engine/ai")
@Tag(name = "AI Graph Controller", description = "Operations for generating DAGs using AI")
public class AiGraphController {

    private static final Logger log = LoggerFactory.getLogger(AiGraphController.class);

    private final AiGraphService aiGraphService;
    
    @Qualifier("graphServicesV1")
    private final GraphInterface objGraphInterface;

    public AiGraphController(AiGraphService aiGraphService, GraphInterface objGraphInterface) {
        this.aiGraphService = aiGraphService;
        this.objGraphInterface = objGraphInterface;
    }

    @Operation(summary = "Create Graph via AI", description = "Translates natural language into a DAG and saves it. Includes auto-correction loop.")
    @PostMapping("/createAIGraph")
    public GraphResponse createAiGraph(@RequestBody AiGraphRequest request) {
        GraphResponse graphResponse = new GraphResponse();
        log.info("AiGraphController.createAiGraph --> request: {}", request);

        try {
            // 1. Generate the candidate graph using AI
            GraphUpdateRequest candidateRequest = aiGraphService.generateGraph(request.getPrompt(), request.getCurrentGraphJson());
            if (candidateRequest.getUserId() == null) {
                candidateRequest.setUserId(request.getUserId() != null ? request.getUserId() : 1L);
            }
            if (request.getGraphId() != null) {
                candidateRequest.setGraphId(request.getGraphId());
                candidateRequest.setUpdateType("C"); // Complex update (structural changes)
            } else {
                if (candidateRequest.getGraphName() != null) {
                    candidateRequest.setGraphName(candidateRequest.getGraphName() + " (" + java.util.UUID.randomUUID().toString().substring(0, 4) + ")");
                }
            }

            // 2. Attempt to upload and validate via our engine
            try {
                if (request.getGraphId() != null) {
                    graphResponse = objGraphInterface.updateGraph(candidateRequest);
                } else {
                    graphResponse = objGraphInterface.uploadUserGraph(candidateRequest);
                }
                
                // If it returns an error in the response body without throwing
                if (graphResponse.getObjErrorDetails() != null && !"0".equals(graphResponse.getObjErrorDetails().getErrorCode())) {
                    throw new RuntimeException(graphResponse.getObjErrorDetails().getErrorMessage());
                }
                
                return graphResponse;

            } catch (Exception validationException) {
                log.warn("Initial AI graph generation failed validation: {}. Triggering self-correction...", validationException.getMessage());
                
                // 3. Self-Correction Loop
                GraphUpdateRequest fixedRequest = aiGraphService.fixGraph(request.getPrompt(), request.getCurrentGraphJson(), candidateRequest, validationException.getMessage());
                if (fixedRequest.getUserId() == null) {
                    fixedRequest.setUserId(request.getUserId() != null ? request.getUserId() : 1L);
                }
                if (request.getGraphId() != null) {
                    fixedRequest.setGraphId(request.getGraphId());
                    fixedRequest.setUpdateType("C");
                } else {
                    if (fixedRequest.getGraphName() != null) {
                        fixedRequest.setGraphName(fixedRequest.getGraphName() + " (" + java.util.UUID.randomUUID().toString().substring(0, 4) + ")");
                    }
                }
                
                // Final attempt to save
                if (request.getGraphId() != null) {
                    graphResponse = objGraphInterface.updateGraph(fixedRequest);
                } else {
                    graphResponse = objGraphInterface.uploadUserGraph(fixedRequest);
                }
                return graphResponse;
            }

        } catch (Exception e) {
            log.error("Exception occurred in createAiGraph: ", e);
            graphResponse.setGraphName("AI Generation Error");
            graphResponse.setGraphDescription("Failed to generate or fix graph from prompt");
            graphResponse.setObjErrorDetails(new ErrorDetails("500", "AI Generation Error: " + e.getMessage()));
        }

        return graphResponse;
    }
}
