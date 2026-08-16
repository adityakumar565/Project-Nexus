package com.workflow.dag_engine.journey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import com.workflow.dag_engine.interfaces.wrapper.GraphInterface;
import com.workflow.dag_engine.interfaces.wrapper.UserInterface;
import com.workflow.dag_engine.models.graph.Cost;
import com.workflow.dag_engine.models.graph.Edge;
import com.workflow.dag_engine.models.graph.GraphMetaData;
import com.workflow.dag_engine.models.graph.GraphResponse;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;
import com.workflow.dag_engine.models.graph.Node;
import com.workflow.dag_engine.models.userModel.UserRequest;
import com.workflow.dag_engine.models.userModel.UserResponse;
import com.workflow.dag_engine.persistence.entities.GraphEntity;
import com.workflow.dag_engine.persistence.repositories.GraphRepository;
import com.workflow.dag_engine.services.UserServices;

@SpringBootTest
public class UserJourneyIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(UserJourneyIntegrationTest.class);

    @Autowired
    @Qualifier("userServicesV1")
    private UserInterface userInterface;

    @Autowired
    @Qualifier("graphServicesV1")
    private GraphInterface graphInterface;

    @Autowired
    private UserServices userServices;

    @Autowired
    private GraphRepository graphRepository;

    @Test
    void testCompleteUserAndGraphLifecycleJourney() throws Exception {

        String testUserName = "journey_user_alice";
        String testUserPass = "AliceSecret123!";

        // Clean up user if already exists from prior run
        if (userServices.validateUser(testUserName, testUserPass)) {
            userServices.deleteUser(testUserName, testUserPass);
        }

        // =========================================================================
        // Step 1: Create a User with Name and Password
        // =========================================================================
        log.info("--- Step 1: Creating User [{}] ---", testUserName);
        UserRequest createUserRequest = new UserRequest(testUserName, testUserPass);
        UserResponse createResponse = userInterface.createUser(createUserRequest);

        assertNotNull(createResponse);
        assertNotNull(createResponse.getObjErrorDetails());
        assertEquals("0", createResponse.getObjErrorDetails().getErrorCode(), "User creation should succeed");

        Long userId = userServices.getUserIdFromNameAndPassword(testUserName, testUserPass);
        assertNotNull(userId, "Created userId should be resolvable from DB");
        log.info("User created successfully with ID: {}", userId);

        // =========================================================================
        // Step 2: Upload / Create a DAG Graph for this User
        // =========================================================================
        log.info("--- Step 2: Uploading Graph for User ID [{}] ---", userId);
        GraphUploadRequest graphUploadRequest = createDiamondGraphRequest(userId, "Commute_Journey_Pipeline");
        GraphResponse uploadResponse = graphInterface.uploadUserGraph(graphUploadRequest);

        assertNotNull(uploadResponse);
        assertNotNull(uploadResponse.getObjErrorDetails());
        assertEquals("0", uploadResponse.getObjErrorDetails().getErrorCode(), "Graph upload should succeed");

        List<GraphEntity> userGraphs = graphRepository.findByUserId(userId);
        assertFalse(userGraphs.isEmpty(), "Graph should be persisted in DB");
        GraphEntity savedEntity = userGraphs.get(0);
        Long graphId = savedEntity.getGraphId();
        String binaryFilePath = savedEntity.getBinaryFilePath();

        assertNotNull(graphId);
        assertNotNull(binaryFilePath);
        assertTrue(new File(binaryFilePath).exists(), "Binary file (.bin) should exist on disk");
        log.info("Graph uploaded with ID [{}] and binary path [{}]", graphId, binaryFilePath);

        // =========================================================================
        // Step 3: Load the Graph (getGraphById)
        // =========================================================================
        log.info("--- Step 3: Loading Graph [{}] into Memory ---", graphId);
        GraphMetaData metaData = new GraphMetaData();
        metaData.setGraphId(graphId);
        metaData.setUserId(userId);
        metaData.setGraphName("Commute_Journey_Pipeline");

        GraphResponse loadResponse1 = graphInterface.getGraphById(metaData);
        assertNotNull(loadResponse1);
        assertEquals("0", loadResponse1.getObjErrorDetails().getErrorCode(), "Initial graph load should succeed");
        assertNotNull(loadResponse1.getObjGraphUploadRequest(), "Should return GraphUploadRequest for UI rendering");
        assertEquals(4, loadResponse1.getObjGraphUploadRequest().getNode().size(), "Should contain 4 nodes");

        // =========================================================================
        // Step 4: Load Again (Checking validation/handling when already in memory)
        // =========================================================================
        log.info("--- Step 4: Loading Graph [{}] Again (Already In Memory) ---", graphId);
        GraphResponse loadResponse2 = graphInterface.getGraphById(metaData);
        assertNotNull(loadResponse2);
        assertEquals("0", loadResponse2.getObjErrorDetails().getErrorCode(), "Loading already active graph should succeed");
        assertNotNull(loadResponse2.getObjGraphUploadRequest(), "Should return GraphUploadRequest from memory/cache");

        // =========================================================================
        // Step 5: Attempt Deleting Graph Before Closing (Should Fail Validation)
        // =========================================================================
        log.info("--- Step 5: Attempting to Delete Graph [{}] While Active in Memory ---", graphId);
        GraphResponse deleteFailResponse = graphInterface.deleteGraph(metaData);
        assertNotNull(deleteFailResponse);
        assertNotNull(deleteFailResponse.getObjErrorDetails());
        assertEquals("2005", deleteFailResponse.getObjErrorDetails().getErrorCode(),
                "Deleting an active in-memory graph should return error 2005");
        assertTrue(deleteFailResponse.getObjErrorDetails().getErrorMessage().contains("currently active in memory"),
                "Error message should explain that the graph must be closed first");

        // Verify graph is still in DB and file still exists
        assertTrue(graphRepository.findById(graphId).isPresent(), "Graph entity must still exist in DB");
        assertTrue(new File(binaryFilePath).exists(), "Binary file must still exist on disk");

        // =========================================================================
        // Step 6: Close / Unload Graph from Memory
        // =========================================================================
        log.info("--- Step 6: Closing / Unloading Graph [{}] ---", graphId);
        GraphResponse closeResponse = graphInterface.closeGraphById(metaData);
        assertNotNull(closeResponse);
        assertEquals("0", closeResponse.getObjErrorDetails().getErrorCode(), "Closing graph should succeed");

        // =========================================================================
        // Step 7: Delete Graph After Closing (Should Succeed)
        // =========================================================================
        log.info("--- Step 7: Deleting Graph [{}] After Closing ---", graphId);
        GraphResponse deleteSuccessResponse = graphInterface.deleteGraph(metaData);
        assertNotNull(deleteSuccessResponse);
        assertEquals("0", deleteSuccessResponse.getObjErrorDetails().getErrorCode(),
                "Deleting closed graph should succeed");

        // Verify DB row deleted and file deleted
        assertTrue(graphRepository.findById(graphId).isEmpty(), "Graph entity must be deleted from DB");
        assertFalse(new File(binaryFilePath).exists(), "Binary file must be deleted from disk");
        log.info("Graph [{}] and binary file [{}] deleted successfully", graphId, binaryFilePath);

        // =========================================================================
        // Step 8: Delete User
        // =========================================================================
        log.info("--- Step 8: Deleting User [{}] ---", userId);
        UserRequest deleteUserRequest = new UserRequest(userId, testUserName, testUserPass);
        UserResponse deleteUserResponse = userInterface.deleteUser(deleteUserRequest);

        assertNotNull(deleteUserResponse);
        assertEquals("0", deleteUserResponse.getObjErrorDetails().getErrorCode(), "Deleting user should succeed");
        assertFalse(userServices.validateUserById(userId), "User should no longer exist in DB");
        log.info("User [{}] deleted successfully. User journey completed!", testUserName);
    }

    private GraphUploadRequest createDiamondGraphRequest(Long userId, String graphName) {
        GraphUploadRequest request = new GraphUploadRequest();
        request.setGraphName(graphName);
        request.setGraphDescription("Diamond DAG test journey");
        request.setUserId(userId);
        request.setStartNodeId(1L);
        request.setCostNames(Arrays.asList("cost", "time"));

        List<Node> nodes = new ArrayList<>();

        Map<String, Double> n1 = new HashMap<>();
        n1.put("cost", 0.0);
        n1.put("time", 0.0);
        nodes.add(new Node(1L, "Home", "Origin", new Cost(n1)));

        Map<String, Double> n2 = new HashMap<>();
        n2.put("cost", 0.0);
        n2.put("time", 5.0);
        nodes.add(new Node(2L, "Bus", "Bus Stop", new Cost(n2)));

        Map<String, Double> n3 = new HashMap<>();
        n3.put("cost", 0.0);
        n3.put("time", 2.0);
        nodes.add(new Node(3L, "Auto", "Auto Stand", new Cost(n3)));

        Map<String, Double> n4 = new HashMap<>();
        n4.put("cost", 0.0);
        n4.put("time", 0.0);
        nodes.add(new Node(4L, "Office", "Destination", new Cost(n4)));

        request.setNode(nodes);

        List<Edge> edges = new ArrayList<>();

        Map<String, Double> e1 = new HashMap<>();
        e1.put("cost", 25.0);
        e1.put("time", 45.0);
        edges.add(new Edge(101L, 1L, 2L, new Cost(e1), "HomeToBus", "Edge from Home to Bus"));

        Map<String, Double> e2 = new HashMap<>();
        e2.put("cost", 160.0);
        e2.put("time", 18.0);
        edges.add(new Edge(102L, 1L, 3L, new Cost(e2), "HomeToAuto", "Edge from Home to Auto"));

        Map<String, Double> e3 = new HashMap<>();
        e3.put("cost", 0.0);
        e3.put("time", 10.0);
        edges.add(new Edge(103L, 2L, 4L, new Cost(e3), "BusToOffice", "Edge from Bus to Office"));

        Map<String, Double> e4 = new HashMap<>();
        e4.put("cost", 0.0);
        e4.put("time", 5.0);
        edges.add(new Edge(104L, 3L, 4L, new Cost(e4), "AutoToOffice", "Edge from Auto to Office"));

        request.setEdge(edges);

        return request;
    }

}
