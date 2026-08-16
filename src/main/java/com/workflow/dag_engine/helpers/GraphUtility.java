package com.workflow.dag_engine.helpers;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflow.dag_engine.impl.UserImpl;
import com.workflow.dag_engine.models.enums.CycleStatus;
import com.workflow.dag_engine.models.enums.GraphStatus;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;
import com.workflow.dag_engine.persistence.entities.GraphEntity;

public class GraphUtility {

    private static final Logger log = LoggerFactory.getLogger(GraphUtility.class);

    static CycleStatus isGraphCyclic(GraphUploadRequest graphUploadRequest) {

        final String methodName = "Inside GraphUtility.isGraphCyclic --> ";

        CycleStatus cycleStatus = CycleStatus.ACYCLIC;

        try {
            log.info(methodName + " graphUploadRequest:" + graphUploadRequest.toString());

            // implement logic to check cycles in graph and change cycic to Y if cycle
            // exists

        } catch (Exception e) {

            log.info(methodName + " Exception occued :" + e.toString());

        }

        return cycleStatus;

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
