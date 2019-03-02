package com.mu.yang.rpc.client;

import com.mu.yang.rpc.entity.Request;

/**
 * Created by yangxianda on 2016/12/18.
 */
public interface Connector {
    ResponseFuture send(Request request);
    void shutdown();
}
