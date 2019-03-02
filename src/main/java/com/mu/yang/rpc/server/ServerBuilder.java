package com.mu.yang.rpc.server;

/**
 * .
 */
public class ServerBuilder {

    private int port;
    private Class clazz;
    private ServerType serverType;
    private String ip = "127.0.0.1";


    public ServerBuilder port(int port) {
        this.port = port;
        return this;
    }

    public ServerBuilder server(ServerType serverType) {
        this.serverType = serverType;
        return this;
    }

    public ServerBuilder bindService(Class clazz) {
        this.clazz = clazz;
        InstanceMap.addClass(clazz);
        return this;
    }

    public void build() {
        switch (serverType) {
            case BIOSERVER:
                new SimpleServer(port);
                break;
            case NIOSERVER:
                new NioServer(ip, port);
                break;
            default:
                new NioServer(ip, port);
        }
    }

    public static enum ServerType {
        NIOSERVER, BIOSERVER;
    }


}

