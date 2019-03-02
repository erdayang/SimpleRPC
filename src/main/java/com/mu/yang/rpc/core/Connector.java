package com.mu.yang.rpc.core;

import com.mu.yang.rpc.connector.ResponseFuture;
import com.mu.yang.rpc.entity.Request;
import org.apache.commons.pool2.PooledObjectFactory;

/**
 * Created by yangxianda on 2016/12/18.
 */
public interface Connector {
    ResponseFuture send(Request request);
    void shutdown();
}
