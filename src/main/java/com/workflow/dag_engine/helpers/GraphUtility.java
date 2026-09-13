package com.workflow.dag_engine.helpers;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflow.dag_engine.impl.UserImpl;
import com.workflow.dag_engine.models.enums.CycleStatus;
import com.workflow.dag_engine.models.enums.GraphStatus;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;
import com.workflow.dag_engine.persistence.entities.GraphEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.workflow.dag_engine.models.graph.Edge;
import com.workflow.dag_engine.models.graph.Node;

public class GraphUtility {

    private static final Logger log = LoggerFactory.getLogger(GraphUtility.class);

    public static CycleStatus isGraphCyclic(GraphUploadRequest graphUploadRequest) {
        if (graphUploadRequest == null) {
            return CycleStatus.ACYCLIC;
        }
        return hasCycle(graphUploadRequest) ? CycleStatus.CYCLIC : CycleStatus.ACYCLIC;
    }

    public static boolean hasCycle(GraphUploadRequest graphUploadRequest) {
        if (graphUploadRequest == null) {
            return false;
        }

        try {
            Map<Long, List<Long>> adj = new HashMap<>();
            Set<Long> allNodes = new HashSet<>();

            // 1. Collect all nodes and their outgoing edges
            if (graphUploadRequest.getNode() != null) {
                for (Node n : graphUploadRequest.getNode()) {
                    if (n != null && n.getId() != null) {
                        allNodes.add(n.getId());
                        adj.putIfAbsent(n.getId(), new ArrayList<>());
                        if (n.getOutgoingEdges() != null) {
                            for (Edge oe : n.getOutgoingEdges()) {
                                if (oe != null && oe.getTargetNodeId() != null) {
                                    allNodes.add(oe.getTargetNodeId());
                                    adj.putIfAbsent(oe.getTargetNodeId(), new ArrayList<>());
                                    adj.get(n.getId()).add(oe.getTargetNodeId());
                                }
                            }
                        }
                    }
                }
            }

            // 2. Collect edges from the edge list
            if (graphUploadRequest.getEdge() != null) {
                for (Edge e : graphUploadRequest.getEdge()) {
                    if (e != null && e.getSourceNodeId() != null && e.getTargetNodeId() != null) {
                        Long u = e.getSourceNodeId();
                        Long v = e.getTargetNodeId();
                        allNodes.add(u);
                        allNodes.add(v);
                        adj.putIfAbsent(u, new ArrayList<>());
                        adj.putIfAbsent(v, new ArrayList<>());
                        if (!adj.get(u).contains(v)) {
                            adj.get(u).add(v);
                        }
                    }
                }
            }

            // 3. Cycle detection using 3-color DFS
            // 0 = UNVISITED, 1 = VISITING (in recursion stack), 2 = VISITED
            Map<Long, Integer> state = new HashMap<>();
            for (Long nodeId : allNodes) {
                state.put(nodeId, 0);
            }

            for (Long nodeId : allNodes) {
                if (state.get(nodeId) == 0) {
                    if (dfsHasCycle(nodeId, adj, state)) {
                        log.warn("Cycle detected in graph '{}' at node {}", graphUploadRequest.getGraphName(), nodeId);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception during cycle detection: ", e);
        }

        return false;
    }

    private static boolean dfsHasCycle(Long current, Map<Long, List<Long>> adj, Map<Long, Integer> state) {
        state.put(current, 1); // Mark as visiting (in recursion stack)

        List<Long> neighbors = adj.get(current);
        if (neighbors != null) {
            for (Long neighbor : neighbors) {
                Integer neighborState = state.getOrDefault(neighbor, 0);
                if (neighborState == 1) {
                    // Back-edge found -> cycle!
                    return true;
                }
                if (neighborState == 0) {
                    if (dfsHasCycle(neighbor, adj, state)) {
                        return true;
                    }
                }
            }
        }

        state.put(current, 2); // Mark as completely visited
        return false;
    }

    public static GraphEntity convertGraphUploadRequestToGraphEntity(GraphUploadRequest graphUploadRequest) {

        final String methodName = "Inside GraphUtility.convertGraphUploadRequestToGraphEntity --> ";

        GraphEntity graphEntity = new GraphEntity();

        try {
            log.info(methodName + " graphUploadRequest:" + graphUploadRequest.toString());

            // implement logic to convert graphUploadRequest to graphEntity and change cycic
            // to Y if cycle exists

            graphEntity.setUserId(graphUploadRequest.getUserId());
            graphEntity.setGraphName(graphUploadRequest.getGraphName());
            graphEntity.setGraphDescription(graphUploadRequest.getGraphDescription());
            graphEntity.setGraphData(graphUploadRequest);
            graphEntity.setNumNodes(graphUploadRequest.getNode() != null ? graphUploadRequest.getNode().size() : 0);
            graphEntity.setNumEdges(graphUploadRequest.getEdge() != null ? graphUploadRequest.getEdge().size() : 0);
            graphEntity.setCostDimension(
                    graphUploadRequest.getCostNames() != null ? graphUploadRequest.getCostNames().size() : 0);
            graphEntity.setIsCyclic(isGraphCyclic(graphUploadRequest));
            graphEntity.setImplementationType("V1_CSR");
            graphEntity.setStatus(GraphStatus.DRAFT);

        } catch (Exception e) {
            log.error(methodName + " Exception occurred: ", e);
        }

        return graphEntity;
    }

    public static com.workflow.dag_engine.models.graph.GraphMetaData convertGraphEntityToMetaData(GraphEntity graphEntity) {
        if (graphEntity == null) {
            return null;
        }

        com.workflow.dag_engine.models.graph.GraphMetaData metaData = new com.workflow.dag_engine.models.graph.GraphMetaData();
        metaData.setGraphId(graphEntity.getGraphId());
        metaData.setGraphName(graphEntity.getGraphName());
        metaData.setGraphDescription(graphEntity.getGraphDescription());
        metaData.setUserId(graphEntity.getUserId());
        metaData.setNumberOfNodes(graphEntity.getNumNodes());
        metaData.setNumberOfEdges(graphEntity.getNumEdges());
        metaData.setCostDimension(graphEntity.getCostDimension());
        metaData.setCycleStatus(graphEntity.getIsCyclic());
        metaData.setGraphStatus(graphEntity.getStatus());
        metaData.setVersion(graphEntity.getVersion());

        return metaData;
    }

    public static java.util.List<com.workflow.dag_engine.models.graph.GraphMetaData> convertGraphEntitiesToMetaDataList(
            java.util.List<GraphEntity> graphEntities) {
        java.util.List<com.workflow.dag_engine.models.graph.GraphMetaData> metaDataList = new java.util.ArrayList<>();
        if (graphEntities != null) {
            for (GraphEntity entity : graphEntities) {
                com.workflow.dag_engine.models.graph.GraphMetaData meta = convertGraphEntityToMetaData(entity);
                if (meta != null) {
                    metaDataList.add(meta);
                }
            }
        }
        return metaDataList;
    }

}
