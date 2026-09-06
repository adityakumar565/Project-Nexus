package com.workflow.dag_engine.interfaces.wrapper;

import com.workflow.dag_engine.models.graph.GraphMetaData;
import com.workflow.dag_engine.models.graph.GraphResponse;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;
import com.workflow.dag_engine.models.graph.GraphUpdateRequest;
import com.workflow.dag_engine.models.graph.UserGraphRequest;
import com.workflow.dag_engine.models.graph.UserGraphResponse;
import com.workflow.dag_engine.models.path.GraphPathResponse;

public interface GraphInterface {

    public GraphResponse uploadUserGraph(GraphUploadRequest graphUploadRequest) throws Exception;

    public GraphResponse getGraphById(GraphMetaData graphMetaData) throws Exception;

    public GraphResponse deleteGraph(GraphMetaData graphMetaData) throws Exception;

    public UserGraphResponse getUserGraphs(UserGraphRequest userGraphRequest) throws Exception;

    public GraphResponse updateGraph(GraphUpdateRequest graphUpdateRequest) throws Exception;

    public GraphResponse closeGraphById(GraphMetaData graphMetaData) throws Exception;

    public GraphPathResponse getGraphPaths(GraphMetaData graphMetaData) throws Exception;

}

