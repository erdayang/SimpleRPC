package com.mu.yang.rpc.chain;

import com.mu.yang.rpc.server.Call;

/**
 * 处理逻辑的节点.
 */
public interface Node {
    public void process(Call call);
}
