package com.example.qr_codescan.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/6/8.
 */
public class Info_bean implements Serializable {
    private String user,appUser,deviceId,signature,operatorId,appPassword;

    public Info_bean(String user, String appUser, String appPassword, String deviceId, String signature, String operatorId) {
        this.user = user;
        this.appUser = appUser;
        this.appPassword=appPassword;
        this.deviceId = deviceId;
        this.signature = signature;
        this.operatorId = operatorId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAppUser() {
        return appUser;
    }

    public void setAppUser(String appUser) {
        this.appUser = appUser;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getAppPassword() {
        return appPassword;
    }

    public void setAppPassword(String appPassword) {
        this.appPassword = appPassword;
    }

    @Override
    public String toString() {
        return "Info_bean{" +
                "user='" + user + '\'' +
                ", appUser='" + appUser + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", signature='" + signature + '\'' +
                ", operatorId='" + operatorId + '\'' +
                ", appPassword='" + appPassword + '\'' +
                '}';
    }
}
