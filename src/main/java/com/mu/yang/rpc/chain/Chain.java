package com.mu.yang.rpc.chain;

import java.util.ArrayList;
import java.util.List;

/**
 * 逻辑处理chain.
 */
public class Chain {

    private List<Node> nodes;

    public Chain() {
        nodes = new ArrayList<Node>();
    }

    public void add(Node node) {
        nodes.add(node);
    }
}
