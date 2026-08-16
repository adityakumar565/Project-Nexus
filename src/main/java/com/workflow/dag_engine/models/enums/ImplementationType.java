package com.workflow.dag_engine.models.enums;

/**
 * Enumeration of supported Graph Storage Kernel Implementation Types.
 * Each type maps to its corresponding Spring Bean identifier.
 */
public enum ImplementationType {

    ADJACENCY_V1("graphComponentManagerAdjacency"),
    CSR_V1("graphComponentManagerCSR");

    private final String beanName;

    ImplementationType(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }

}
