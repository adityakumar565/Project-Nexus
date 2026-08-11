package com.workflow.dag_engine.interfaces.wrapper;

import com.workflow.dag_engine.models.graph.GraphResponse;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;

public interface GraphInterface {

    public GraphResponse uploadUserGraph(GraphUploadRequest graphUploadRequest) throws Exception;

    public GraphResponse getGraphById(String userId, Long graphId) throws Exception;

    public GraphResponse deleteGraph(String userId, Long graphId) throws Exception;

}
