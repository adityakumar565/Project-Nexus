package com.workflow.dag_engine.models.userModel;

import com.workflow.dag_engine.models.validation.BaseModel;

public class UserRequest extends BaseModel {

    Long userId;
    String userName;
    String userPassword;

    public UserRequest(Long userId) {
        this.userId = userId;
    }

    public UserRequest(String userName, String userPassword) {
        this.userName = userName;
        this.userPassword = userPassword;
    }

    public UserRequest(Long userId, String userName, String userPassword) {
        this.userId = userId;
        this.userName = userName;
        this.userPassword = userPassword;
    }

    public UserRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    @Override
    public String toString() {
        return "[UserRequest(userId=" + userId + ", userName=" + userName + ", userPassword=" + userPassword + ")]";
    }

}
