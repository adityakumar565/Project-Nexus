package com.workflow.dag_engine.componentManager;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.workflow.dag_engine.componentManager.graphs.component_manager.GraphComponentManagerImpl;
import com.workflow.dag_engine.interfaces.componentManager.GraphComponentManagerInterface;
import com.workflow.dag_engine.models.graph.Cost;
import com.workflow.dag_engine.models.graph.Edge;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;
import com.workflow.dag_engine.models.graph.Node;
import org.springframework.beans.factory.annotation.Qualifier;

@SpringBootTest
public class GraphAdjacencyComponentManagerTest {

    @Autowired
    @Qualifier("graphComponentManagerAdjacency")
    private GraphComponentManagerInterface graphComponentManager;

    @Autowired
    private com.workflow.dag_engine.componentManager.graphs.factory.GraphComponentManagerFactory componentManagerFactory;

    private GraphUploadRequest createDiamondGraphRequest() {
        GraphUploadRequest request = new GraphUploadRequest();
        request.setGraphName("Commute_Route_Optimization");
        request.setGraphDescription("Diamond graph evaluating commute options (Bus vs Auto) to Office");
        request.setUserId(10L);
        request.setStartNodeId(1L);
        request.setCostNames(Arrays.asList("cost", "time"));

        List<Node> nodes = new ArrayList<>();

        Map<String, Double> n1Map = new HashMap<>();
        n1Map.put("cost", 0.0);
        n1Map.put("time", 0.0);
        nodes.add(new Node(1L, "Start / Home", "Origin location", new Cost(n1Map)));

        Map<String, Double> n2Map = new HashMap<>();
        n2Map.put("cost", 0.0);
        n2Map.put("time", 5.0);
        nodes.add(new Node(2L, "Bus", "Public bus stop", new Cost(n2Map)));

        Map<String, Double> n3Map = new HashMap<>();
        n3Map.put("cost", 0.0);
        n3Map.put("time", 2.0);
        nodes.add(new Node(3L, "Auto", "Auto stand", new Cost(n3Map)));

        Map<String, Double> n4Map = new HashMap<>();
        n4Map.put("cost", 0.0);
        n4Map.put("time", 0.0);
        nodes.add(new Node(4L, "Office", "Destination office", new Cost(n4Map)));

        request.setNode(nodes);

        List<Edge> edges = new ArrayList<>();

        Map<String, Double> e1Map = new HashMap<>();
        e1Map.put("cost", 25.0);
        e1Map.put("time", 45.0);
        edges.add(new Edge(101L, 1L, 2L, new Cost(e1Map), "Home to Bus Stop", "Take the bus"));

        Map<String, Double> e2Map = new HashMap<>();
        e2Map.put("cost", 160.0);
        e2Map.put("time", 18.0);
        edges.add(new Edge(102L, 1L, 3L, new Cost(e2Map), "Home to Auto Stand", "Take an auto"));

        Map<String, Double> e3Map = new HashMap<>();
        e3Map.put("cost", 0.0);
        e3Map.put("time", 7.0);
        edges.add(new Edge(103L, 2L, 4L, new Cost(e3Map), "Bus Stop to Office", "Walk to office"));

        Map<String, Double> e4Map = new HashMap<>();
        e4Map.put("cost", 0.0);
        e4Map.put("time", 2.0);
        edges.add(new Edge(104L, 3L, 4L, new Cost(e4Map), "Auto Drop to Office", "Walk to office"));

        request.setEdge(edges);
        return request;
    }

    @Test
    public void testTransformSaveAndLoadAdjacencyGraph() {
        GraphUploadRequest request = createDiamondGraphRequest();

        // 1. Transform and store graph
        String savedPath = graphComponentManager.transformAndStoreGraph(request);
        assertNotNull(savedPath);
        assertTrue(savedPath.contains("10_Commute_Route_Optimization.bin"));

        File file = new File(savedPath);
        assertTrue(file.exists(), "Binary file must exist on disk");
        assertTrue(file.length() > 0, "Binary file must not be empty");

        // 2. Test in-memory queries
        assertEquals(1, graphComponentManager.getStartNodeId());
        assertEquals(4, graphComponentManager.getNumberOfNodes());
        assertEquals(4, graphComponentManager.getNumberOfEdges());
        assertEquals(2, graphComponentManager.getCostDimension());

        // Sink test
        int[] sinks = graphComponentManager.getSinkNodes();
        assertEquals(1, sinks.length);
        assertEquals(4, sinks[0]);

        // Degrees
        assertEquals(0, graphComponentManager.getInDegree(1));
        assertEquals(2, graphComponentManager.getOutDegree(1));
        assertEquals(1, graphComponentManager.getInDegree(2));
        assertEquals(1, graphComponentManager.getOutDegree(2));
        assertEquals(2, graphComponentManager.getInDegree(4));
        assertEquals(0, graphComponentManager.getOutDegree(4));

        // Traversal children & parents
        int[] childrenOf1 = graphComponentManager.getChildren(1);
        Arrays.sort(childrenOf1);
        assertArrayEquals(new int[] { 2, 3 }, childrenOf1);

        int[] parentsOf4 = graphComponentManager.getParents(4);
        Arrays.sort(parentsOf4);
        assertArrayEquals(new int[] { 2, 3 }, parentsOf4);

        // Edge ID queries
        assertEquals(101, graphComponentManager.getEdgeId(1, 2));
        assertEquals(102, graphComponentManager.getEdgeId(1, 3));
        assertEquals(103, graphComponentManager.getEdgeId(2, 4));
        assertEquals(104, graphComponentManager.getEdgeId(3, 4));
        assertEquals(-1, graphComponentManager.getEdgeId(1, 4));

        // Weights
        assertArrayEquals(new float[] { 0.0f, 5.0f }, graphComponentManager.getNodeWeight(2), 0.001f);
        assertArrayEquals(new float[] { 25.0f, 45.0f }, graphComponentManager.getEdgeWeight(101), 0.001f);
        assertArrayEquals(new float[] { 160.0f, 18.0f }, graphComponentManager.getEdgeWeight(1, 3), 0.001f);

        // 3. Unload and Load from file to test binary deserialization
        graphComponentManager.unload();
        assertEquals(0, graphComponentManager.getNumberOfNodes());

        graphComponentManager.loadGraph(savedPath);
        assertEquals(1, graphComponentManager.getStartNodeId());
        assertEquals(4, graphComponentManager.getNumberOfNodes());
        assertEquals(4, graphComponentManager.getNumberOfEdges());
        assertEquals(2, graphComponentManager.getCostDimension());

        int[] loadedChildren1 = graphComponentManager.getChildren(1);
        Arrays.sort(loadedChildren1);
        assertArrayEquals(new int[] { 2, 3 }, loadedChildren1);

        assertArrayEquals(new float[] { 25.0f, 45.0f }, graphComponentManager.getEdgeWeight(101), 0.001f);
        assertArrayEquals(new float[] { 160.0f, 18.0f }, graphComponentManager.getEdgeWeight(1, 3), 0.001f);

        // 4. Test ImplementationType and Factory Resolution
        assertEquals(com.workflow.dag_engine.models.enums.ImplementationType.ADJACENCY_V1, graphComponentManager.getImplementationType());

        GraphComponentManagerInterface factoryResolved = componentManagerFactory.getComponentManager("ADJACENCY_V1");
        assertNotNull(factoryResolved);
        assertEquals(com.workflow.dag_engine.models.enums.ImplementationType.ADJACENCY_V1, factoryResolved.getImplementationType());
    }

}
