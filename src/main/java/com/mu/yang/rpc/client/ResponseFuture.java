package com.mu.yang.rpc.client;

import com.mu.yang.rpc.entity.Request;
import com.mu.yang.rpc.entity.Response;

public class ResponseFuture {
    private Request request = null;
    private Response response = null;
    public ResponseFuture(Request request){
        this.request = request;
    }

    public synchronized void done(Response response){
        this.response = response;
        this.notifyAll();
    }

    public synchronized Object get(){
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return process(response);
    }

    public Object get(long timeout){
        try {
            this.wait(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return process(response);
    }

    public Object process(Response response){
        return response.getResult();
    }
}
