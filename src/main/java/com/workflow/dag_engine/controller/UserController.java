package com.workflow.dag_engine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.workflow.dag_engine.interfaces.wrapper.UserInterface;
import com.workflow.dag_engine.models.userModel.UserRequest;
import com.workflow.dag_engine.models.userModel.UserResponse;

@RestController
@RequestMapping("/workflow-engine/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Qualifier("userServicesV1")
    private UserInterface userImplementation;

    public UserController(UserInterface userImplementation) {
        this.userImplementation = userImplementation;
    }

    @PostMapping("/create")
    public UserResponse createUser(@RequestBody UserRequest userRequest) throws Exception {

        log.info("REST request to create user: {}", userRequest != null ? userRequest.getUserName() : "null");
        return userImplementation.createUser(userRequest);

    }

    @PostMapping("/delete")
    public UserResponse deleteUser(@RequestBody UserRequest userRequest) throws Exception {

        return userImplementation.deleteUser(userRequest);

    }

    @PostMapping("/validate")
    public UserResponse validateUser(@RequestBody UserRequest userRequest) throws Exception {

        return userImplementation.validateUser(userRequest);

    }

}
