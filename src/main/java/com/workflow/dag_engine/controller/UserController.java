package com.workflow.dag_engine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.workflow.dag_engine.interfaces.wrapper.UserInterface;
import com.workflow.dag_engine.models.graph.UserGraphResponse;
import com.workflow.dag_engine.models.userModel.UserRequest;
import com.workflow.dag_engine.models.userModel.UserResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/workflow-engine/user")
@Tag(name = "User Controller", description = "Operations for user registration, validation, deletion, and graph retrieval")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Qualifier("userServicesV1")
    private UserInterface userImplementation;

    public UserController(UserInterface userImplementation) {
        this.userImplementation = userImplementation;
    }

    @Operation(summary = "Create a new user", description = "Registers a new user in the workflow system")
    @PostMapping("/create")
    public UserResponse createUser(@RequestBody UserRequest userRequest) throws Exception {

        log.info("REST request to create user: {}", userRequest != null ? userRequest.getUserName() : "null");
        return userImplementation.createUser(userRequest);

    }

    @Operation(summary = "Delete an existing user", description = "Deletes a user based on user name and password credentials")
    @PostMapping("/delete")
    public UserResponse deleteUser(@RequestBody UserRequest userRequest) throws Exception {

        return userImplementation.deleteUser(userRequest);

    }

    @Operation(summary = "Validate user credentials", description = "Validates user existence by user ID or username and password")
    @PostMapping("/validate")
    public UserResponse validateUser(@RequestBody UserRequest userRequest) throws Exception {

        return userImplementation.validateUser(userRequest);

    }

    @Operation(summary = "Fetch user graphs", description = "Retrieves a list of all graph metadata belonging to the specified user")
    @PostMapping({"/get", "/graphs"})
    public UserGraphResponse getUserGraph(@RequestBody UserRequest userRequest) throws Exception {

        log.info("REST request to fetch user graphs for: {}", userRequest);
        return userImplementation.getUserGraph(userRequest);

    }

}
