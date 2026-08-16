package com.workflow.dag_engine.interfaces.wrapper;

import com.workflow.dag_engine.models.graph.UserGraphResponse;
import com.workflow.dag_engine.models.userModel.UserRequest;
import com.workflow.dag_engine.models.userModel.UserResponse;

public interface UserInterface {

    public UserResponse createUser(UserRequest userRequest) throws Exception;

    public UserResponse deleteUser(UserRequest userRequest) throws Exception;

    public UserResponse validateUser(UserRequest userRequest) throws Exception;

    public UserGraphResponse getUserGraph(UserRequest userRequest) throws Exception;

}