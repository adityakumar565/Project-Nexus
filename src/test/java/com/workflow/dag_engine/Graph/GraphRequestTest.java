package com.workflow.dag_engine.Graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.workflow.dag_engine.impl.GraphImpl;
import com.workflow.dag_engine.impl.UserImpl;
import com.workflow.dag_engine.models.graph.Cost;
import com.workflow.dag_engine.models.graph.Edge;
import com.workflow.dag_engine.models.graph.GraphUploadRequest;
import com.workflow.dag_engine.models.graph.Node;
import com.workflow.dag_engine.models.graph.UserGraphResponse;
import com.workflow.dag_engine.models.userModel.UserRequest;

@SpringBootTest
public class GraphRequestTest {

    @Autowired
    private GraphImpl graphImpl;

    @Autowired
    private UserImpl userImpl;

    @Test
    void testGetUserGraphNonExistentUser() throws Exception {
        UserRequest userRequest = new UserRequest(-999L);
        UserGraphResponse response = userImpl.getUserGraph(userRequest);
        assertNotNull(response);
        assertNotNull(response.getObjErrorDetails());
        assertEquals("10", response.getObjErrorDetails().getErrorCode());
        assertEquals("User Not Available", response.getObjErrorDetails().getErrorMessage());
    }

}

