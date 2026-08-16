package com.workflow.dag_engine.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflow.dag_engine.impl.UserImpl;
import com.workflow.dag_engine.models.userModel.UserRequest;
import com.workflow.dag_engine.persistence.entities.UserEntity;

public class UserUtility {

    private static final Logger log = LoggerFactory.getLogger(UserImpl.class);

    public static UserEntity convertUserRequestToEntity(UserRequest userRequest) {

        log.info("Inside UserEntity.UserEntity -> Input is : -> " + userRequest.toString());

        UserEntity userEntity = new UserEntity();
        if (userRequest.getUserId() != null) {
            userEntity.setUserId(userRequest.getUserId());
        }
        userEntity.setUserName(userRequest.getUserName());
        userEntity.setPassword(userRequest.getUserPassword());

        log.info("Inside UserEntity.UserEntity -> Output is : -> " + userEntity.toString());

        return userEntity;

    }

    public static boolean isNotNullOrEmptyObject(Object obj) {

        if (obj != null) {
            return true;
        }
        return false;

    }

}
