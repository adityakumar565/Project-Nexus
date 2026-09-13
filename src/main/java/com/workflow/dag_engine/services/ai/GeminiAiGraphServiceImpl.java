package com.workflow.dag_engine.services.ai;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.dag_engine.interfaces.ai.AiGraphService;
import com.workflow.dag_engine.models.graph.GraphUpdateRequest;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;

@Service
@ConditionalOnProperty(name = "dag.ai.provider", havingValue = "gemini", matchIfMissing = true)
public class GeminiAiGraphServiceImpl implements AiGraphService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiGraphServiceImpl.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${dag.ai.gemini.api-key}")
    private String apiKey;

    @Value("${dag.ai.gemini.model}")
    private String modelName;

    @Value("${dag.ai.gemini.base-url}")
    private String baseUrl;

    public GeminiAiGraphServiceImpl(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().build();
        this.objectMapper = objectMapper;
    }

    private static final String SYSTEM_PROMPT = """
            You are an expert workflow architect. Convert the user's workflow description into a valid Directed Acyclic Graph (DAG) JSON.
            Output ONLY valid JSON matching this exact structure, with no markdown formatting or markdown ticks.

            Contract:
            {
                "graphName": "string",
                "graphDescription": "string",
                "startNodeId": integer,
                "costNames": ["<metric1>", "<metric2>", "..."], // Extract metrics from user prompt (e.g., ["time", "cost", "latency", "fun"])
                "node": [
                    {
                        "id": integer,
                        "name": "string",
                        "nodeCost": {
                            "costVector": {
                                "<metric1>": double,
                                "<metric2>": double
                            }
                        }
                    }
                ],
                "edge": [
                    {
                        "id": integer,
                        "sourceNodeId": integer,
                        "targetNodeId": integer,
                        "edgeCost": {
                            "costVector": {
                                "<metric1>": double,
                                "<metric2>": double
                            }
                        }
                    }
                ]
            }

            Rules:
            1. No cycles allowed.
            2. Nodes must have sequential IDs starting from 1.
            3. Edges must connect sourceNodeId to targetNodeId.
            4. Do not include markdown code blocks (```json).
            """;

    @Override
    public GraphUpdateRequest generateGraph(String prompt, String currentGraphJson) throws Exception {
        log.info("Generating/Updating Graph with Gemini for prompt: {}", prompt);
        String fullPrompt = SYSTEM_PROMPT + "\n\nUser Request: " + prompt;
        if (currentGraphJson != null && !currentGraphJson.trim().isEmpty()) {
            fullPrompt += "\n\nNOTE: You are MODIFYING an existing graph. Keep the same node IDs and edges where appropriate, and only make changes described in the prompt. Current Graph JSON:\n" + currentGraphJson;
        }
        return callGemini(fullPrompt);
    }

    @Override
    public GraphUpdateRequest fixGraph(String prompt, String currentGraphJson, GraphUpdateRequest invalidGraph, String errorMessage) throws Exception {
        log.info("Fixing Graph with Gemini due to error: {}", errorMessage);
        String invalidJson = objectMapper.writeValueAsString(invalidGraph);
        
        String fixPrompt = SYSTEM_PROMPT + "\n\n" +
                "The following JSON graph was generated for the user request: '" + prompt + "', " +
                "but it failed validation with this error: " + errorMessage + "\n\n" +
                "Current Invalid JSON:\n" + invalidJson + "\n\n";
                
        if (currentGraphJson != null && !currentGraphJson.trim().isEmpty()) {
            fixPrompt += "Original Base Graph JSON before modifications:\n" + currentGraphJson + "\n\n";
        }

        fixPrompt += "Please fix the error (e.g. remove the cycle or fix the dangling edge) and output the corrected JSON.";

        return callGemini(fixPrompt);
    }

    private GraphUpdateRequest callGemini(String fullPrompt) throws Exception {
        // Construct the Gemini API Payload
        Map<String, Object> payload = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", fullPrompt)
                        })
                },
                "generationConfig", Map.of(
                        "responseMimeType", "application/json"
                )
        );

        String uri = baseUrl + "/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;

        String responseStr = restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);

        // Parse Gemini Response Structure
        JsonNode rootNode = objectMapper.readTree(responseStr);
        String generatedJson = rootNode.path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();

        // Clean up possible markdown ticks if the model ignored the instruction
        generatedJson = generatedJson.replaceAll("```json", "").replaceAll("```", "").trim();

        // Deserialize to our DTO
        GraphUpdateRequest request = objectMapper.readValue(generatedJson, GraphUpdateRequest.class);
        
        // Ensure updateType is set appropriately by the controller later, or set a default
        if (request.getUpdateType() == null) {
            request.setUpdateType("C"); // Default to complex update for AI changes
        }
        
        return request;
    }
}
