package com.mu.yang.rpc.server;

import com.mu.yang.rpc.entity.Response;

/**
 * 一次请求.
 */
public class Call {
    private byte[] data;
    private Response response;
    private Connection connection;
    public Call(){

    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
