package com.mu.yang.rpc.entity;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by yangxianda on 2016/12/18.
 */
public class Request implements Serializable{
    private String id;
    private byte[] data;
    private String clazz;
    private String method;
    private Class<?>[] paramType;
    private Object[] params;
    private boolean debug = false;
    private Protocol protocol;

    public Request(){
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public Class<?>[] getParamType() {
        return paramType;
    }

    public void setParamType(Class<?>[] paramType) {
        this.paramType = paramType;
    }

    public String toString(){
        return JSON.toJSONString(this);
    }

    public static enum Protocol {
        JAVA((byte) 0x0), PROTO((byte) 0x01);
        byte flag;

        private Protocol(byte flag) {
            this.flag = flag;
        }
    }
}
