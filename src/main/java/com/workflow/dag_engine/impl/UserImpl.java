package com.workflow.dag_engine.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.workflow.dag_engine.helpers.UserUtility;
import com.workflow.dag_engine.interfaces.wrapper.UserInterface;
import com.workflow.dag_engine.models.userModel.UserRequest;
import com.workflow.dag_engine.models.userModel.UserResponse;
import com.workflow.dag_engine.models.validation.ApplicationException;
import com.workflow.dag_engine.models.validation.ErrorDetails;
import com.workflow.dag_engine.persistence.entities.UserEntity;
import com.workflow.dag_engine.services.UserServices;

@Component("userServicesV1")
public class UserImpl implements UserInterface {

    private static final Logger log = LoggerFactory.getLogger(UserImpl.class);

    private final UserServices userServices;

    public UserImpl(UserServices userServices) {
        this.userServices = userServices;
    }

    @Override
    public UserResponse createUser(UserRequest userRequest) throws Exception {

        log.info("createUser called for user: {}", userRequest != null ? userRequest.getUserName() : "null");

        ErrorDetails objDetails = new ErrorDetails(1);
        UserResponse userResponse = new UserResponse(objDetails, userRequest);

        try {
            UserEntity userEntity = UserUtility.convertUserRequestToEntity(userRequest);

            if (UserUtility.isNotNullOrEmptyObject(userRequest)) {

                if (userRequest.getUserName().isEmpty()) {
                    throw new ApplicationException("10", "User Name Cannot be Empty");
                }

                if (userRequest.getUserPassword().isEmpty()) {
                    throw new ApplicationException("11", "User Password Cannot be Empty");
                }

                userServices.createUser(userEntity);

            }

        } catch (ApplicationException e) {
            objDetails.setErrorCode(e.code);
            objDetails.setErrorMessage(e.message);
            userResponse.setObjErrorDetails(objDetails);
        } catch (Exception e) {
            objDetails.setErrorCode("500");
            objDetails.setErrorMessage(e.getMessage());
            userResponse.setObjErrorDetails(objDetails);
        }

        return userResponse;

    }

    @Override
    public UserResponse deleteUser(UserRequest userRequest) throws Exception {

        log.info("deleteUser called for user: {}", userRequest != null ? userRequest.getUserName() : "null");

        ErrorDetails objDetails = new ErrorDetails(1);
        UserResponse userResponse = new UserResponse(objDetails, userRequest);

        try {
            UserEntity userEntity = UserUtility.convertUserRequestToEntity(userRequest);

            if (UserUtility.isNotNullOrEmptyObject(userRequest)) {

                if (userRequest.getUserName().isEmpty()) {
                    throw new ApplicationException("10", "User Name Cannot be Empty");
                }

                if (userRequest.getUserPassword().isEmpty()) {
                    throw new ApplicationException("11", "User Password Cannot be Empty");
                }

                Long userId = userServices.getUserIdFromNameAndPassword(userEntity.getUserName(),
                        userEntity.getPassword());

                if (!UserUtility.isNotNullOrEmptyObject(userId)) {
                    throw new ApplicationException("10", "User Not Available");
                }

                userEntity.setUserId(userId);
                userServices.deleteUser(userEntity);

            }

        } catch (ApplicationException e) {
            objDetails.setErrorCode(e.code);
            objDetails.setErrorMessage(e.message);
            userResponse.setObjErrorDetails(objDetails);
        } catch (Exception e) {
            objDetails.setErrorCode("500");
            objDetails.setErrorMessage(e.getMessage());
            userResponse.setObjErrorDetails(objDetails);
        }

        return userResponse;

    }

    public UserResponse validateUser(UserRequest userRequest) {

        ErrorDetails objDetails = new ErrorDetails(1);
        UserResponse userResponse = new UserResponse(objDetails, userRequest);

        try {
            UserEntity userEntity = UserUtility.convertUserRequestToEntity(userRequest);

            if (UserUtility.isNotNullOrEmptyObject(userRequest)) {

                if (userRequest.getUserName().isEmpty()) {
                    throw new ApplicationException("10", "User Name Cannot be Empty");
                }

                if (userRequest.getUserPassword().isEmpty()) {
                    throw new ApplicationException("11", "User Password Cannot be Empty");
                }

                Long userId = userServices.getUserIdFromNameAndPassword(userEntity.getUserName(),
                        userEntity.getPassword());

                if (!UserUtility.isNotNullOrEmptyObject(userId)) {
                    throw new ApplicationException("10", "User Not Available");
                }

                userEntity.setUserId(userId);

            }

        } catch (ApplicationException e) {
            objDetails.setErrorCode(e.code);
            objDetails.setErrorMessage(e.message);
            userResponse.setObjErrorDetails(objDetails);
        } catch (Exception e) {
            objDetails.setErrorCode("500");
            objDetails.setErrorMessage(e.getMessage());
            userResponse.setObjErrorDetails(objDetails);
        }

        return userResponse;

    }

}
