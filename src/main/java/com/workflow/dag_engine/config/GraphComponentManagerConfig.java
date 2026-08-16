package com.workflow.dag_engine.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.workflow.dag_engine.componentManager.graphs.component_manager.GraphComponentManagerImpl;
import com.workflow.dag_engine.componentManager.graphs.interfaces.kernels.GraphBridgeInterface;
import com.workflow.dag_engine.componentManager.graphs.interfaces.kernels.GraphUtilityInterface;
import com.workflow.dag_engine.interfaces.componentManager.GraphComponentManagerInterface;

@Configuration
public class GraphComponentManagerConfig {

    /**
     * Adjacency Matrix Graph Component Manager bean.
     * Injects the Adjacency Bridge and Adjacency Utility kernel set.
     */
    @Bean("graphComponentManagerAdjacency")
    @Primary
    public GraphComponentManagerInterface graphComponentManagerAdjacency(
            @Qualifier("graphAdjacencyBridge") GraphBridgeInterface graphBridge,
            @Qualifier("graphAdjacencyUtility") GraphUtilityInterface graphUtility) {
        return new GraphComponentManagerImpl(graphBridge, graphUtility);
    }

    /**
     * Alias for graphComponentManagerV1 pointing to the default Adjacency kernel.
     */
    @Bean("graphComponentManagerV1")
    public GraphComponentManagerInterface graphComponentManagerV1(
            @Qualifier("graphAdjacencyBridge") GraphBridgeInterface graphBridge,
            @Qualifier("graphAdjacencyUtility") GraphUtilityInterface graphUtility) {
        return new GraphComponentManagerImpl(graphBridge, graphUtility);
    }

}
