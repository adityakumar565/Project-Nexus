package com.workflow.dag_engine.Graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.workflow.dag_engine.helpers.GraphUtility;
import com.workflow.dag_engine.models.enums.CycleStatus;
import com.workflow.dag_engine.models.graph.Edge;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;
import com.workflow.dag_engine.models.graph.Node;

public class GraphUtilityTest {

    @Test
    void testAcyclicLinearGraph() {
        GraphUploadRequest req = new GraphUploadRequest();
        req.setGraphName("Linear DAG");
        req.setNode(Arrays.asList(new Node(1L, "A"), new Node(2L, "B"), new Node(3L, "C")));
        
        Edge e1 = new Edge(1L, 1L, 2L, null, "A->B", "");
        Edge e2 = new Edge(2L, 2L, 3L, null, "B->C", "");
        req.setEdge(Arrays.asList(e1, e2));

        assertFalse(GraphUtility.hasCycle(req));
        assertEquals(CycleStatus.ACYCLIC, GraphUtility.isGraphCyclic(req));
    }

    @Test
    void testAcyclicDiamondGraph() {
        GraphUploadRequest req = new GraphUploadRequest();
        req.setGraphName("Diamond DAG");
        req.setNode(Arrays.asList(new Node(1L, "Start"), new Node(2L, "Left"), new Node(3L, "Right"), new Node(4L, "End")));

        Edge e1 = new Edge(1L, 1L, 2L, null, "Start->Left", "");
        Edge e2 = new Edge(2L, 1L, 3L, null, "Start->Right", "");
        Edge e3 = new Edge(3L, 2L, 4L, null, "Left->End", "");
        Edge e4 = new Edge(4L, 3L, 4L, null, "Right->End", "");
        req.setEdge(Arrays.asList(e1, e2, e3, e4));

        assertFalse(GraphUtility.hasCycle(req));
        assertEquals(CycleStatus.ACYCLIC, GraphUtility.isGraphCyclic(req));
    }

    @Test
    void testTwoNodeCycle() {
        GraphUploadRequest req = new GraphUploadRequest();
        req.setGraphName("Two Node Cycle");
        req.setNode(Arrays.asList(new Node(1L, "A"), new Node(2L, "B")));

        Edge e1 = new Edge(1L, 1L, 2L, null, "A->B", "");
        Edge e2 = new Edge(2L, 2L, 1L, null, "B->A", "");
        req.setEdge(Arrays.asList(e1, e2));

        assertTrue(GraphUtility.hasCycle(req));
        assertEquals(CycleStatus.CYCLIC, GraphUtility.isGraphCyclic(req));
    }

    @Test
    void testSelfLoopCycle() {
        GraphUploadRequest req = new GraphUploadRequest();
        req.setGraphName("Self Loop");
        req.setNode(Arrays.asList(new Node(1L, "A")));

        Edge e1 = new Edge(1L, 1L, 1L, null, "A->A", "");
        req.setEdge(Arrays.asList(e1));

        assertTrue(GraphUtility.hasCycle(req));
        assertEquals(CycleStatus.CYCLIC, GraphUtility.isGraphCyclic(req));
    }

    @Test
    void testThreeNodeCycle() {
        GraphUploadRequest req = new GraphUploadRequest();
        req.setGraphName("Three Node Cycle");
        req.setNode(Arrays.asList(new Node(1L, "A"), new Node(2L, "B"), new Node(3L, "C")));

        Edge e1 = new Edge(1L, 1L, 2L, null, "A->B", "");
        Edge e2 = new Edge(2L, 2L, 3L, null, "B->C", "");
        Edge e3 = new Edge(3L, 3L, 1L, null, "C->A", "");
        req.setEdge(Arrays.asList(e1, e2, e3));

        assertTrue(GraphUtility.hasCycle(req));
        assertEquals(CycleStatus.CYCLIC, GraphUtility.isGraphCyclic(req));
    }

    @Test
    void testDisconnectedGraphWithCycleInSecondComponent() {
        GraphUploadRequest req = new GraphUploadRequest();
        req.setGraphName("Disconnected with Cycle");
        req.setNode(Arrays.asList(
            new Node(1L, "A1"), new Node(2L, "A2"),
            new Node(10L, "B1"), new Node(20L, "B2")
        ));

        // Component A is DAG: 1 -> 2
        Edge e1 = new Edge(1L, 1L, 2L, null, "A1->A2", "");
        // Component B is cyclic: 10 -> 20 -> 10
        Edge e2 = new Edge(2L, 10L, 20L, null, "B1->B2", "");
        Edge e3 = new Edge(3L, 20L, 10L, null, "B2->B1", "");
        req.setEdge(Arrays.asList(e1, e2, e3));

        assertTrue(GraphUtility.hasCycle(req));
        assertEquals(CycleStatus.CYCLIC, GraphUtility.isGraphCyclic(req));
    }

    @Test
    void testEmptyAndNull() {
        assertFalse(GraphUtility.hasCycle(null));
        assertEquals(CycleStatus.ACYCLIC, GraphUtility.isGraphCyclic(null));

        GraphUploadRequest empty = new GraphUploadRequest();
        assertFalse(GraphUtility.hasCycle(empty));
        assertEquals(CycleStatus.ACYCLIC, GraphUtility.isGraphCyclic(empty));
    }
}
