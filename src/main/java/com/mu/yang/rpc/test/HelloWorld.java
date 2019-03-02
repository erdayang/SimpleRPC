package com.mu.yang.rpc.test;

/**
 * .
 */
public class HelloWorld implements IHelloWorld {
    public String get(String name) {
        return "this is an proxy test";
    }

    public int add(Integer a, Integer b) {
        return a + b;
    }


}
