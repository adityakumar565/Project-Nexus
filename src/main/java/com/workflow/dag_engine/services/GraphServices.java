package com.workflow.dag_engine.services;

import org.springframework.stereotype.Service;

import com.workflow.dag_engine.persistence.entities.GraphEntity;
import com.workflow.dag_engine.persistence.repositories.GraphRepository;

@Service
public class GraphServices {

    private final GraphRepository graphRepository;

    public GraphServices(GraphRepository graphRepository) {
        this.graphRepository = graphRepository;
    }

    public GraphEntity createGraph(GraphEntity graph) {
        return graphRepository.save(graph);
    }

    public GraphEntity deleteGraph(GraphEntity graph) {
        if (graphRepository.existsByGraphName(graph.getGraphName())) {
            graphRepository.delete(graph);
            return graph;
        }
        return null;
    }

    public GraphEntity updateGraph(GraphEntity graph) {
        if (graphRepository.existsByGraphName(graph.getGraphName())) {
            return graphRepository.save(graph);
        }
        return null;
    }

}
