package com.workflow.dag_engine.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.workflow.dag_engine.persistence.entities.UserEntity;
import com.workflow.dag_engine.persistence.repositories.UserRepository;

@Service
public class UserServices {

    private static final Logger log = LoggerFactory.getLogger(UserServices.class);

    private final UserRepository userRepository;

    public UserServices(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity createUser(UserEntity user) {

        log.info("Creating user: {}", user != null ? user.getUserName() : "null");

        if (userRepository.existsByUserName(user.getUserName())) {
            throw new RuntimeException("User already exists.");
        }
        return userRepository.save(user);
    }

    public boolean validateUser(String userName, String password) {

        return userRepository.existsByUserNameAndPassword(userName, password);

    }

    public boolean deleteUser(String userName, String userPassword) {
        log.info("Inside  UserService.deleteUser -> Input is : " + userName + " " + userPassword);
        try {
            if (validateUser(userName, userPassword)) {
                userRepository.delete(userRepository.findByUserNameAndPassword(userName, userPassword));
                return true;
            }
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage(), e);
        }
        return false;
    }

    public UserEntity deleteUser(UserEntity userEntity) {

        if (userRepository.existsByUserName(userEntity.getUserName())) {
            userRepository.delete(userEntity);
            return userEntity;
        }
        return null;
    }

    public Long getUserIdFromNameAndPassword(String userName, String password) {

        if (validateUser(userName, password)) {
            return userRepository.fetchUserIdByNameAndPassword(userName, password);
        }
        return null;
    }

}
