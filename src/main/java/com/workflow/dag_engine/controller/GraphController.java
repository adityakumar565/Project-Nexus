package com.workflow.dag_engine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;

import com.workflow.dag_engine.interfaces.wrapper.GraphInterface;
import com.workflow.dag_engine.models.graph.GraphMetaData;
import com.workflow.dag_engine.models.graph.GraphResponse;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;
import com.workflow.dag_engine.models.graph.GraphUpdateRequest;
import com.workflow.dag_engine.models.path.GraphPathResponse;
import com.workflow.dag_engine.models.validation.ErrorDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/workflow-engine/graphs")
@Tag(name = "Graph Controller", description = "Operations for DAG graphs, saving, and execution")
public class GraphController {

    private static final Logger log = LoggerFactory.getLogger(GraphController.class);

    @Qualifier("graphServicesV1")
    private GraphInterface objGraphInterface;

    public GraphController(GraphInterface objGraphInterface) {
        this.objGraphInterface = objGraphInterface;
    }

    @Operation(summary = "Upload and save a DAG graph", description = "Validates user, converts nodes/edges into graph entities and persists in DB")
    @PostMapping("/upload")
    public GraphResponse saveGraph(@RequestBody GraphUploadRequest objGraphUploadRequest) {

        GraphResponse graphResponse = new GraphResponse();
        log.info("GraphController.saveGraph --> graphUploadRequest: " + objGraphUploadRequest.toString());

        try {

            graphResponse = objGraphInterface.uploadUserGraph(objGraphUploadRequest);

        } catch (Exception e) {
            log.error("Exception occurred in saveGraph: ", e);
            graphResponse.setGraphName("Error in saving graph: " + objGraphUploadRequest.getGraphName());
            graphResponse.setGraphDescription("Error");
            graphResponse.setObjErrorDetails(new ErrorDetails("500", "Error in saving Graph Response"));
        }

        return graphResponse;

    }

    @Operation(summary = "Update an existing DAG graph", description = "Performs simple (metadata) or complex (structural) updates to an existing graph")
    @PostMapping("/update")
    public GraphResponse updateGraph(@RequestBody GraphUpdateRequest request) {

        GraphResponse graphResponse = new GraphResponse();
        log.info("GraphController.updateGraph --> request: " + request);

        try {

            graphResponse = objGraphInterface.updateGraph(request);

        } catch (Exception e) {
            log.error("Exception occurred in updateGraph: ", e);
            graphResponse.setGraphName("Error in updating graph: " + (request != null ? request.getGraphName() : ""));
            graphResponse.setGraphDescription("Error");
            graphResponse.setObjErrorDetails(new ErrorDetails("500", "Error in updating Graph Response: " + e.getMessage()));
        }

        return graphResponse;

    }

    @Operation(summary = "Load and get graph by ID", description = "Loads graph into memory and returns graph details")
    @PostMapping("/get")
    public GraphResponse getGraphById(@RequestBody GraphMetaData graphMetaData) {

        GraphResponse graphResponse = new GraphResponse();
        log.info("GraphController.getGraphById --> graphMetaData: " + graphMetaData);

        try {

            graphResponse = objGraphInterface.getGraphById(graphMetaData);

        } catch (Exception e) {
            log.error("Exception occurred in getGraphById: ", e);
            graphResponse.setGraphName(graphMetaData != null ? graphMetaData.getGraphName() : "Error");
            graphResponse.setGraphDescription("Error");
            graphResponse.setObjErrorDetails(new ErrorDetails("500", "Error in getting Graph: " + e.getMessage()));
        }

        return graphResponse;

    }

    @Operation(summary = "Close and unload graph from memory", description = "Unloads the active in-memory graph")
    @PostMapping("/close")
    public GraphResponse closeGraphById(@RequestBody GraphMetaData graphMetaData) {

        GraphResponse graphResponse = new GraphResponse();
        log.info("GraphController.closeGraphById --> graphMetaData: " + graphMetaData);

        try {

            graphResponse = objGraphInterface.closeGraphById(graphMetaData);

        } catch (Exception e) {
            log.error("Exception occurred in closeGraphById: ", e);
            graphResponse.setGraphName(graphMetaData != null ? graphMetaData.getGraphName() : "Error");
            graphResponse.setGraphDescription("Error");
            graphResponse.setObjErrorDetails(new ErrorDetails("500", "Error in closing Graph: " + e.getMessage()));
        }

        return graphResponse;

    }

    @Operation(summary = "Delete a DAG graph", description = "Deletes graph from DB and disk if not currently active in memory")
    @PostMapping("/delete")
    public GraphResponse deleteGraph(@RequestBody GraphMetaData graphMetaData) {

        GraphResponse graphResponse = new GraphResponse();
        log.info("GraphController.deleteGraph --> graphMetaData: " + graphMetaData);

        try {

            graphResponse = objGraphInterface.deleteGraph(graphMetaData);

        } catch (Exception e) {
            log.error("Exception occurred in deleteGraph: ", e);
            graphResponse.setGraphName(graphMetaData != null ? graphMetaData.getGraphName() : "Error");
            graphResponse.setGraphDescription("Error");
            graphResponse.setObjErrorDetails(new ErrorDetails("500", "Error in deleting Graph: " + e.getMessage()));
        }

        return graphResponse;

    }

    @Operation(summary = "Calculate and retrieve all paths for a graph", description = "Returns all possible paths in the DAG along with cumulative costs")
    @PostMapping("/paths")
    public GraphPathResponse getGraphPaths(@RequestBody GraphMetaData graphMetaData) {

        GraphPathResponse response = new GraphPathResponse();
        log.info("GraphController.getGraphPaths --> graphMetaData: " + graphMetaData);

        try {
            response = objGraphInterface.getGraphPaths(graphMetaData);
        } catch (Exception e) {
            log.error("Exception occurred in getGraphPaths: ", e);
            response.setGraphId(graphMetaData != null ? graphMetaData.getGraphId() : null);
            response.setGraphName(graphMetaData != null ? graphMetaData.getGraphName() : "Error");
            response.setObjErrorDetails(new ErrorDetails("500", "Error in calculating paths: " + e.getMessage()));
        }

        return response;
    }

    @Operation(summary = "Calculate and retrieve all paths for a graph by ID", description = "Returns all possible paths in the DAG along with cumulative costs")
    @GetMapping("/{graphId}/paths")
    public GraphPathResponse getGraphPathsById(@PathVariable("graphId") Long graphId) {

        GraphMetaData metaData = new GraphMetaData();
        metaData.setGraphId(graphId);
        return getGraphPaths(metaData);
    }

}

