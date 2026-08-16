package com.workflow.dag_engine.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.workflow.dag_engine.helpers.UserUtility;
import com.workflow.dag_engine.interfaces.wrapper.GraphInterface;
import com.workflow.dag_engine.interfaces.wrapper.UserInterface;
import com.workflow.dag_engine.models.graph.UserGraphRequest;
import com.workflow.dag_engine.models.graph.UserGraphResponse;
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
    private final GraphInterface objGraphInterface;

    public UserImpl(UserServices userServices, @Lazy @Qualifier("graphServicesV1") GraphInterface objGraphInterface) {
        this.userServices = userServices;
        this.objGraphInterface = objGraphInterface;
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

    @Override
    public UserResponse validateUser(UserRequest userRequest) {

        ErrorDetails objDetails = new ErrorDetails(1);
        UserResponse userResponse = new UserResponse(objDetails, userRequest);

        try {
            if (!UserUtility.isNotNullOrEmptyObject(userRequest)) {
                throw new ApplicationException("200", "User Request Cannot be Empty");
            }

            // If userId is provided, validate existence by ID
            if (userRequest.getUserId() != null) {
                boolean exists = userServices.validateUserById(userRequest.getUserId());
                if (!exists) {
                    throw new ApplicationException("10", "User Not Available");
                }
            } else {
                // Otherwise validate by userName & password
                if (userRequest.getUserName() == null || userRequest.getUserName().isEmpty()) {
                    throw new ApplicationException("10", "User Name Cannot be Empty");
                }

                if (userRequest.getUserPassword() == null || userRequest.getUserPassword().isEmpty()) {
                    throw new ApplicationException("11", "User Password Cannot be Empty");
                }

                UserEntity userEntity = UserUtility.convertUserRequestToEntity(userRequest);
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

    @Override
    public UserGraphResponse getUserGraph(UserRequest userRequest) throws Exception {

        String methodName = "Inside UserImpl.getUserGraph --> ";
        log.info(methodName + " userRequest:" + userRequest);

        UserGraphResponse userGraphResponse = new UserGraphResponse();

        try {
            if (!UserUtility.isNotNullOrEmptyObject(userRequest)) {
                userGraphResponse.setObjErrorDetails(new ErrorDetails("200", "User Request Cannot be Empty"));
                return userGraphResponse;
            }

            Long targetUserId = userRequest.getUserId();

            // If userId is not provided, resolve it from userName & password
            if (targetUserId == null) {
                if (userRequest.getUserName() == null || userRequest.getUserName().isEmpty()
                        || userRequest.getUserPassword() == null || userRequest.getUserPassword().isEmpty()) {
                    userGraphResponse.setObjErrorDetails(new ErrorDetails("200", "User ID or Username/Password required"));
                    return userGraphResponse;
                }
                targetUserId = userServices.getUserIdFromNameAndPassword(userRequest.getUserName(),
                        userRequest.getUserPassword());
            }

            // Validate user existence
            if (targetUserId == null || !userServices.validateUserById(targetUserId)) {
                userGraphResponse.setObjErrorDetails(new ErrorDetails("10", "User Not Available"));
                return userGraphResponse;
            }

            // Delegate to GraphInterface to maintain loose coupling
            UserGraphRequest userGraphRequest = new UserGraphRequest(targetUserId);
            userGraphResponse = objGraphInterface.getUserGraphs(userGraphRequest);

        } catch (Exception e) {
            log.error(methodName + " Exception occurred: ", e);
            userGraphResponse.setObjErrorDetails(new ErrorDetails("500", e.getMessage()));
        }

        return userGraphResponse;
    }

}
