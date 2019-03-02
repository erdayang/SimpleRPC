package com.mu.yang.rpc.client;

/**
 * 用于选择connector的策略
 * Created by yangxianda on 2016/12/23.
 */
public interface Strategy {
    Connector choose();
}
