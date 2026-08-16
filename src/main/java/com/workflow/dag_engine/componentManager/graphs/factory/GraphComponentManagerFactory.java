package com.workflow.dag_engine.componentManager.graphs.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.workflow.dag_engine.interfaces.componentManager.GraphComponentManagerInterface;
import com.workflow.dag_engine.models.enums.ImplementationType;

@Component
public class GraphComponentManagerFactory {

    private static final Logger log = LoggerFactory.getLogger(GraphComponentManagerFactory.class);

    private final ApplicationContext applicationContext;

    public GraphComponentManagerFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Resolves the GraphComponentManager bean matching the specified ImplementationType.
     * 
     * @param type implementation type enum
     * @return corresponding GraphComponentManagerInterface instance
     */
    public GraphComponentManagerInterface getComponentManager(ImplementationType type) {
        if (type == null) {
            log.warn("Null implementation type provided, falling back to ADJACENCY_V1");
            type = ImplementationType.ADJACENCY_V1;
        }

        try {
            return (GraphComponentManagerInterface) applicationContext.getBean(type.getBeanName());
        } catch (Exception ex) {
            log.error("Failed to resolve component manager bean for '{}', falling back to primary", type, ex);
            return applicationContext.getBean(GraphComponentManagerInterface.class);
        }
    }

    /**
     * Resolves the GraphComponentManager bean matching the specified implementation type string.
     * 
     * @param typeStr implementation type string from DB
     * @return corresponding GraphComponentManagerInterface instance
     */
    public GraphComponentManagerInterface getComponentManager(String typeStr) {
        if (typeStr == null || typeStr.trim().isEmpty()) {
            return getComponentManager(ImplementationType.ADJACENCY_V1);
        }

        try {
            ImplementationType type = ImplementationType.valueOf(typeStr.trim().toUpperCase());
            return getComponentManager(type);
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown implementation type '{}' in DB, falling back to ADJACENCY_V1", typeStr);
            return getComponentManager(ImplementationType.ADJACENCY_V1);
        }
    }

}
