package com.workflow.dag_engine.componentManager.graphs.kernels.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.workflow.dag_engine.componentManager.graphs.DataContract.GraphDataContract;
import com.workflow.dag_engine.componentManager.graphs.interfaces.kernels.GraphBridgeInterface;
import com.workflow.dag_engine.componentManager.graphs.interfaces.kernels.GraphStorageInterface;
import com.workflow.dag_engine.componentManager.graphs.interfaces.kernels.GraphUtilityInterface;
import com.workflow.dag_engine.models.enums.ImplementationType;
import com.workflow.dag_engine.models.graph.Edge;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;
import com.workflow.dag_engine.models.graph.Node;

@Component("graphAdjacencyBridge")
public class GraphAdjacencyBridge implements GraphBridgeInterface {

    private static final Logger log = LoggerFactory.getLogger(GraphAdjacencyBridge.class);
    private static final int MAGIC_NUMBER = 0x4441474D; // "DAGM"
    private static final int BINARY_VERSION = 1;

    private final GraphUtilityInterface graphUtility;

    public GraphAdjacencyBridge(@Qualifier("graphAdjacencyUtility") GraphUtilityInterface graphUtility) {
        this.graphUtility = graphUtility;
    }

    @Override
    public ImplementationType getImplementationType() {
        return ImplementationType.ADJACENCY_V1;
    }

    @Override
    public boolean bake(GraphUploadRequest request, GraphStorageInterface storageInterface) {
        if (request == null || !(storageInterface instanceof GraphAdjacencyStorage)) {
            log.error("Invalid bake request or incompatible storage type");
            return false;
        }

        GraphAdjacencyStorage storage = (GraphAdjacencyStorage) storageInterface;
        storage.setUserId(request.getUserId());
        storage.setGraphName(request.getGraphName());

        List<Node> nodes = request.getNode() != null ? request.getNode() : new ArrayList<>();
        List<Edge> edges = request.getEdge() != null ? request.getEdge() : new ArrayList<>();
        List<String> costNames = request.getCostNames() != null ? request.getCostNames() : new ArrayList<>();

        int n = nodes.size();
        int e = edges.size();
        int k = costNames.size();

        int[] indexToNodeId = new int[n];
        Map<Integer, Integer> nodeIdToIndex = new HashMap<>(n);
        float[] flatNodeCosts = new float[n * k];

        for (int i = 0; i < n; i++) {
            Node node = nodes.get(i);
            int nodeId = node.getId().intValue();
            indexToNodeId[i] = nodeId;
            nodeIdToIndex.put(nodeId, i);
            float[] cVec = graphUtility.extractCostVector(node.getNodeCost(), costNames);
            if (cVec != null) {
                System.arraycopy(cVec, 0, flatNodeCosts, i * k, Math.min(k, cVec.length));
            }
        }

        int[][] adjMatrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            java.util.Arrays.fill(adjMatrix[i], -1);
        }

        int[] inDegree = new int[n];
        int[] outDegree = new int[n];

        int[] indexToEdgeId = new int[e];
        int[] edgeSource = new int[e];
        int[] edgeTarget = new int[e];
        Map<Integer, Integer> edgeIdToIndex = new HashMap<>(e);
        float[] flatEdgeCosts = new float[e * k];

        for (int j = 0; j < e; j++) {
            Edge edge = edges.get(j);
            int edgeId = edge.getId().intValue();
            int srcId = edge.getSourceNodeId().intValue();
            int dstId = edge.getTargetNodeId().intValue();

            indexToEdgeId[j] = edgeId;
            edgeSource[j] = srcId;
            edgeTarget[j] = dstId;
            edgeIdToIndex.put(edgeId, j);

            Integer u = nodeIdToIndex.get(srcId);
            Integer v = nodeIdToIndex.get(dstId);

            if (u != null && v != null) {
                adjMatrix[u][v] = j; // Dense edge index (0..E-1)
                outDegree[u]++;
                inDegree[v]++;
            }

            float[] eVec = graphUtility.extractCostVector(edge.getEdgeCost(), costNames);
            if (eVec != null) {
                System.arraycopy(eVec, 0, flatEdgeCosts, j * k, Math.min(k, eVec.length));
            }
        }

        List<Integer> sinks = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (outDegree[i] == 0) {
                sinks.add(indexToNodeId[i]);
            }
        }
        int[] sinkNodes = sinks.stream().mapToInt(Integer::intValue).toArray();

        storage.setIndexToNodeId(indexToNodeId);
        storage.setNodeIdToIndex(nodeIdToIndex);
        storage.setAdjMatrix(adjMatrix);
        storage.setInDegreeArray(inDegree);
        storage.setOutDegreeArray(outDegree);
        storage.setIndexToEdgeId(indexToEdgeId);
        storage.setEdgeSource(edgeSource);
        storage.setEdgeTarget(edgeTarget);
        storage.setEdgeIdToIndex(edgeIdToIndex);
        storage.setFlatNodeCosts(flatNodeCosts);
        storage.setFlatEdgeCosts(flatEdgeCosts);
        storage.setSinkNodes(sinkNodes);

        log.info("Successfully baked graph '{}' (nodes={}, edges={}, dims={}) into Adjacency Storage",
                request.getGraphName(), n, e, k);
        return true;
    }

    @Override
    public String save(GraphStorageInterface storageInterface) {
        if (!(storageInterface instanceof GraphAdjacencyStorage)) {
            throw new IllegalArgumentException("Incompatible storage type for GraphAdjacencyBridge");
        }

        GraphAdjacencyStorage storage = (GraphAdjacencyStorage) storageInterface;
        String filePath = graphUtility.resolveStoragePath(storage.getUserId(), storage.getGraphName());
        File file = new File(filePath);

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        GraphDataContract contract = storage.getGraphDataContract();
        int n = contract != null ? contract.getNumNodes() : 0;
        int e = contract != null ? contract.getNumEdges() : 0;
        int k = contract != null ? contract.getCostDimension() : 0;
        int startNodeId = contract != null ? contract.getStartNodeId() : 0;

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            // Header
            out.writeInt(MAGIC_NUMBER);
            out.writeInt(BINARY_VERSION);
            out.writeInt(n);
            out.writeInt(e);
            out.writeInt(k);
            out.writeInt(startNodeId);
            out.writeLong(storage.getUserId() != null ? storage.getUserId() : 0L);
            out.writeUTF(storage.getGraphName() != null ? storage.getGraphName() : "");

            // Index to NodeId
            int[] indexToNodeId = storage.getIndexToNodeId();
            for (int i = 0; i < n; i++) {
                out.writeInt(indexToNodeId != null && i < indexToNodeId.length ? indexToNodeId[i] : 0);
            }

            // Adjacency Matrix (N x N)
            int[][] adjMatrix = storage.getAdjMatrix();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    out.writeInt(adjMatrix != null && i < adjMatrix.length && j < adjMatrix[i].length ? adjMatrix[i][j] : -1);
                }
            }

            // Edge Tables
            int[] indexToEdgeId = storage.getIndexToEdgeId();
            int[] edgeSource = storage.getEdgeSource();
            int[] edgeTarget = storage.getEdgeTarget();
            for (int i = 0; i < e; i++) {
                out.writeInt(indexToEdgeId != null && i < indexToEdgeId.length ? indexToEdgeId[i] : 0);
                out.writeInt(edgeSource != null && i < edgeSource.length ? edgeSource[i] : 0);
                out.writeInt(edgeTarget != null && i < edgeTarget.length ? edgeTarget[i] : 0);
            }

            // Flat Node Costs (N x K)
            float[] flatNodeCosts = storage.getFlatNodeCosts();
            int totalNodeFloats = n * k;
            for (int i = 0; i < totalNodeFloats; i++) {
                out.writeFloat(flatNodeCosts != null && i < flatNodeCosts.length ? flatNodeCosts[i] : 0.0f);
            }

            // Flat Edge Costs (E x K)
            float[] flatEdgeCosts = storage.getFlatEdgeCosts();
            int totalEdgeFloats = e * k;
            for (int i = 0; i < totalEdgeFloats; i++) {
                out.writeFloat(flatEdgeCosts != null && i < flatEdgeCosts.length ? flatEdgeCosts[i] : 0.0f);
            }

            // Sinks
            int[] sinkNodes = storage.getEndNodes();
            out.writeInt(sinkNodes != null ? sinkNodes.length : 0);
            if (sinkNodes != null) {
                for (int sink : sinkNodes) {
                    out.writeInt(sink);
                }
            }

            out.flush();
            storage.setBinaryFilePath(filePath);
            log.info("Saved binary graph to '{}' (file size={} bytes)", filePath, file.length());
            return filePath;
        } catch (IOException ex) {
            log.error("Failed to save binary graph to {}", filePath, ex);
            throw new RuntimeException("Failed to save binary graph: " + ex.getMessage(), ex);
        }
    }

    @Override
    public GraphStorageInterface load(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Binary graph file does not exist: " + filePath);
        }

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            int magic = in.readInt();
            if (magic != MAGIC_NUMBER) {
                throw new IllegalStateException("Invalid binary graph file format (magic number mismatch)");
            }

            int version = in.readInt();
            int n = in.readInt();
            int e = in.readInt();
            int k = in.readInt();
            int startNodeId = in.readInt();
            long userId = in.readLong();
            String graphName = in.readUTF();

            GraphDataContract contract = new GraphDataContract(n, e, k, startNodeId);
            GraphAdjacencyStorage storage = new GraphAdjacencyStorage(contract);
            storage.setUserId(userId);
            storage.setGraphName(graphName);
            storage.setBinaryFilePath(filePath);

            int[] indexToNodeId = new int[n];
            Map<Integer, Integer> nodeIdToIndex = new HashMap<>(n);
            for (int i = 0; i < n; i++) {
                int nodeId = in.readInt();
                indexToNodeId[i] = nodeId;
                nodeIdToIndex.put(nodeId, i);
            }

            int[][] adjMatrix = new int[n][n];
            int[] inDegree = new int[n];
            int[] outDegree = new int[n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    int edgeId = in.readInt();
                    adjMatrix[i][j] = edgeId;
                    if (edgeId != -1) {
                        outDegree[i]++;
                        inDegree[j]++;
                    }
                }
            }

            int[] indexToEdgeId = new int[e];
            int[] edgeSource = new int[e];
            int[] edgeTarget = new int[e];
            Map<Integer, Integer> edgeIdToIndex = new HashMap<>(e);
            for (int i = 0; i < e; i++) {
                int edgeId = in.readInt();
                int src = in.readInt();
                int dst = in.readInt();
                indexToEdgeId[i] = edgeId;
                edgeSource[i] = src;
                edgeTarget[i] = dst;
                edgeIdToIndex.put(edgeId, i);
            }

            float[] flatNodeCosts = new float[n * k];
            for (int i = 0; i < n * k; i++) {
                flatNodeCosts[i] = in.readFloat();
            }

            float[] flatEdgeCosts = new float[e * k];
            for (int i = 0; i < e * k; i++) {
                flatEdgeCosts[i] = in.readFloat();
            }

            int sinkCount = in.readInt();
            int[] sinkNodes = new int[sinkCount];
            for (int i = 0; i < sinkCount; i++) {
                sinkNodes[i] = in.readInt();
            }

            storage.setIndexToNodeId(indexToNodeId);
            storage.setNodeIdToIndex(nodeIdToIndex);
            storage.setAdjMatrix(adjMatrix);
            storage.setInDegreeArray(inDegree);
            storage.setOutDegreeArray(outDegree);
            storage.setIndexToEdgeId(indexToEdgeId);
            storage.setEdgeSource(edgeSource);
            storage.setEdgeTarget(edgeTarget);
            storage.setEdgeIdToIndex(edgeIdToIndex);
            storage.setFlatNodeCosts(flatNodeCosts);
            storage.setFlatEdgeCosts(flatEdgeCosts);
            storage.setSinkNodes(sinkNodes);

            log.info("Loaded binary graph '{}' (nodes={}, edges={}, dims={}) from '{}'",
                    graphName, n, e, k, filePath);
            return storage;
        } catch (IOException ex) {
            log.error("Failed to read binary graph from {}", filePath, ex);
            throw new RuntimeException("Failed to read binary graph: " + ex.getMessage(), ex);
        }
    }

}
