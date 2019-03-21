package com.mu.yang.rpc.server;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
        connections = new LinkedBlockingQueue<>();
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

    public void doReadLoop() throws InterruptedException {
        LOGGER.debug(this.getName() + "start...");
        while (true) {
            SelectionKey key = null;

            //int size = connections.size();
            int size = connections.size();
            for (int i = size; i > 0; i--) {
                Connection connection = connections.take(); // 这里是必须的
                try {
                    connection.channel.register(readSelector, SelectionKey.OP_READ, connection);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            }
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
        byte[] data = connection.read();
        LOGGER.error("data_length: " + data.length);
        Call call = new Call();
        call.setConnection(connection);
        call.setData(data);
        try {
            requestQueue.put(call);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            doReadLoop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                readSelector.close();
            } catch (IOException ioe) {
                LOGGER.error("Error closing read selector in " + Thread.currentThread().getName());
            }
        }
    }

}
