package com.mopote.mps.web.webbean;

import java.io.Serializable;

/**
 * Created by wangshu.yang on 2015/3/27.
 */
public class ResponseBean implements Serializable{
    private int code;
    private String message;

    public ResponseBean(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
