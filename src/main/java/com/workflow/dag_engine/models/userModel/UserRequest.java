package com.workflow.dag_engine.models.userModel;

import com.workflow.dag_engine.models.validation.BaseModel;

public class UserRequest extends BaseModel {

    String userName;
    String userPassword;

    public UserRequest(String userName, String userPassword) {
        this.userName = userName;
        this.userPassword = userPassword;
    }

    public UserRequest() {
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
        return "[UserRequest(userName=" + userName + ", userPassword=" + userPassword + ")]";
    }

}
