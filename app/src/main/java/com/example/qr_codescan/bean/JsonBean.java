package com.example.qr_codescan.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/7/6.
 */
public class JsonBean implements Serializable {
    private String status;
    private String message;

    public JsonBean(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "JsonBean{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
