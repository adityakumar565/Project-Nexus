package com.workflow.dag_engine.componentManager.graphs.interfaces.kernels;

import com.workflow.dag_engine.models.enums.ImplementationType;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;

public interface GraphBridgeInterface {

    public boolean bake(GraphUploadRequest objGraphUploadRequest, GraphStorageInterface objGraphStorage);

    public String save(GraphStorageInterface objGraphStorage);

    public GraphStorageInterface load(String filePath);

    public ImplementationType getImplementationType();

}
