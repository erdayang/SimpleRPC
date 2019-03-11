package com.mu.yang.rpc.server;


import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;

/**
 * 基于java NIO 实现的 rpc server端.
 * Listener 用于监听 client连接，主要是创建connection
 * Reader用于监听数据接入，主要是Selector.OP_READ，并创建call，放入requestQueue中
 * Handler 监听requestQueue，并真正的处理数据，包括解码-业务处理-编码返回.
 *
 * 首先学习的 Hadoop的IPC，大部分代码是从那里copy过来的.
 */
public class NioServer {

    public static final Logger LOGGER = Logger.getLogger(NioServer.class);

    private Listener listener = null;
    // 收到消息之后的队列，给予 handler处理
    private BlockingQueue<Call> requestQueue = new LinkedBlockingQueue();
    // 消息处理后的队列，用于返回
    //   private BlockingQueue responseQueue = new LinkedBlockingQueue();

    private Handler[] handlers;
    private int HANDLER_COUNT = 10;

    private final String ip;
    private final int port;

    public NioServer(String ip, int port){
        this.ip = ip;
        this.port = port;
        initHandler();
        try {
            listener = new Listener(ip, port, requestQueue);
        } catch (IOException e) {
            e.printStackTrace();
        }
        listener.start();
        LOGGER.info(String.format("create an NioServer: ip=%s, port=%d", ip, port));
    }

    public void initHandler() {
        handlers = new Handler[HANDLER_COUNT];
        for (int i = 0; i < HANDLER_COUNT; i++) {
            handlers[i] = new Handler("HandlerThread-" + i, requestQueue);
            handlers[i].start();
        }
    }


}
