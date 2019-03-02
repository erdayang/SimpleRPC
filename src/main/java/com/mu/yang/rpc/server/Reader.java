package com.mu.yang.rpc.server;

import com.mu.yang.rpc.entity.Request;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;

/**
 * 读数据,解码.
 * 一个reader处理多个connection，并注册读事件.
 */
public class Reader extends Thread {

    public static final Logger LOGGER = Logger.getLogger(Reader.class);

    private final Selector readSelector;
    private BlockingQueue<Connection> connections;
    private BlockingQueue<Call> requestQueue;

    public Reader(String name, BlockingQueue<Call> requestQueue) throws IOException {
        super(name);
        this.requestQueue = requestQueue;
        readSelector = Selector.open();
        connections = new LinkedBlockingQueue<Connection>();
        LOGGER.info("create " + name);
    }

    public void addConnection(Connection connection) throws InterruptedException {
        connections.put(connection);
        readSelector.wakeup();
        try {
            connection.channel.register(readSelector, SelectionKey.OP_READ, connection);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }

        LOGGER.debug(this.getName() + ": register read selector..." + connections.size());
    }

    public void doReadLoop() {
        LOGGER.debug(this.getName() + "start...");
        while (true) {
            SelectionKey key = null;

            int size = connections.size();
            LOGGER.debug("connection size: " + size);
            try {
                readSelector.select();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Iterator<SelectionKey> iterator = readSelector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                key = iterator.next();
                iterator.remove();
                if (key.isValid() && key.isReadable()) {
                    try {
                        doRead(key);
                    } catch (IOException e) {
                        Connection tmp = (Connection)key.attachment();
                        LOGGER.info(String.format("离开：%s:%d", tmp.getHostAddr(), tmp.getPort()));
                        key.cancel();
                    }
                }
            }


        }
    }

    public void doRead(SelectionKey key) throws IOException {
        Connection connection = (Connection) key.attachment();
        if (connection == null) {
            LOGGER.debug("connection is null");
            return;
        }
        Request request = connection.readAndProcess();
        Call call = new Call();
        call.setConnection(connection);
        call.setRequest(request);
        try {
            requestQueue.put(call);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            doReadLoop();
        } finally {
            try {
                readSelector.close();
            } catch (IOException ioe) {
                LOGGER.error("Error closing read selector in " + Thread.currentThread().getName());
            }
        }
    }

}
