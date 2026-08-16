package com.workflow.dag_engine.componentManager.graphs.kernels.impl;

import java.io.File;
import java.util.List;

import org.springframework.stereotype.Component;

import com.workflow.dag_engine.componentManager.graphs.DataContract.GraphDataContract;
import com.workflow.dag_engine.componentManager.graphs.interfaces.kernels.GraphUtilityInterface;
import com.workflow.dag_engine.models.graph.Cost;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;

@Component("graphAdjacencyUtility")
public class GraphAdjacencyUtility implements GraphUtilityInterface {

    public static final String STORAGE_DIR = "graph_representation" + File.separator + "component_representation";

    @Override
    public GraphDataContract extractDataContract(GraphUploadRequest request) {
        if (request == null) {
            return new GraphDataContract(0, 0, 0, 0);
        }

        int numNodes = request.getNode() != null ? request.getNode().size() : 0;
        int numEdges = request.getEdge() != null ? request.getEdge().size() : 0;
        int costDimension = request.getCostNames() != null ? request.getCostNames().size() : 0;

        int startNodeId = 0;
        if (request.getStartNodeId() != null) {
            startNodeId = request.getStartNodeId().intValue();
        } else if (request.getNode() != null && !request.getNode().isEmpty()) {
            startNodeId = request.getNode().get(0).getId().intValue();
        }

        return new GraphDataContract(numNodes, numEdges, costDimension, startNodeId);
    }

    @Override
    public float[] extractCostVector(Cost cost, List<String> costNames) {
        if (costNames == null || costNames.isEmpty()) {
            return new float[0];
        }

        float[] vector = new float[costNames.size()];
        for (int i = 0; i < costNames.size(); i++) {
            String name = costNames.get(i);
            if (cost != null && cost.getCostVector() != null && cost.getCostVector().containsKey(name)) {
                Double val = cost.getCostAt(name);
                vector[i] = val != null ? val.floatValue() : 0.0f;
            } else {
                vector[i] = 0.0f;
            }
        }
        return vector;
    }

    @Override
    public String resolveStoragePath(Long userId, String graphName) {
        String sanitizedGraphName = graphName != null ? graphName.replaceAll("[^a-zA-Z0-9._-]", "_") : "graph";
        long uid = userId != null ? userId : 0L;
        return STORAGE_DIR + File.separator + uid + "_" + sanitizedGraphName + ".bin";
    }

}
