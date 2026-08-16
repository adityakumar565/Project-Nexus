package com.workflow.dag_engine.componentManager.graphs.interfaces.kernels;

import java.util.List;

import com.workflow.dag_engine.componentManager.graphs.DataContract.GraphDataContract;
import com.workflow.dag_engine.models.graph.Cost;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;

public interface GraphUtilityInterface {

    public GraphDataContract extractDataContract(GraphUploadRequest request);

    public float[] extractCostVector(Cost cost, List<String> costNames);

    public String resolveStoragePath(Long userId, String graphName);

}
