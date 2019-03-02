package com.mu.yang.rpc.test;

import com.mu.yang.rpc.server.ServerBuilder;
import com.mu.yang.rpc.server.ServerBuilder.ServerType;
import java.io.IOException;

/**
 * .
 */
public class ServerTest {

    public static void main(String[] args) throws IOException {
        ServerBuilder builder = new ServerBuilder();
        builder.port(8080)
            .bindService(HelloWorld.class)
            .server(ServerType.NIOSERVER)
            .build();
    }
}
