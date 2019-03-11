package com.mu.yang.rpc.server;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 监听客户端的连接.
 * 如果key是acceptable,创建connection,交给Reader.
 */
public class Listener extends Thread {

    public static final Logger LOGGER = Logger.getLogger(Listener.class);

    private ServerSocketChannel acceptChannel = null;
    private Selector selecor = null;
    private InetSocketAddress address = null;
    private AtomicInteger readerRotater = new AtomicInteger(0);
    private Reader[] readers = null;
    private int READER_COUNT = 3;
    private BlockingQueue<Call> requestQueue;

    public Listener(String ip, int port, BlockingQueue<Call> requestQueue) throws IOException {
        super("Thread-listener");
        this.requestQueue = requestQueue;
        address = new InetSocketAddress(ip, port);
        acceptChannel = ServerSocketChannel.open();
        acceptChannel.configureBlocking(false);
        ServerSocket socket = acceptChannel.socket();
        socket.bind(address);
        port = socket.getLocalPort();
        readers = new Reader[READER_COUNT];
        for (int i = 0; i < READER_COUNT; i++) {
            readers[i] = new Reader("ReaderThread-" + i, requestQueue);
            readers[i].start();
        }
        selecor = Selector.open();
        acceptChannel.register(selecor, SelectionKey.OP_ACCEPT);
    }

    synchronized Selector getSelecor() {
        return selecor;
    }

    public Reader getReader() {
        int idx = Math.abs(readerRotater.incrementAndGet() % readers.length);
        return readers[idx];
    }

    public void doAccept(SelectionKey key) throws IOException, InterruptedException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel channel;
        while ((channel = server.accept()) != null) {
            channel.configureBlocking(false);
            channel.socket().setTcpNoDelay(false);
            channel.socket().setKeepAlive(true);
            Connection connection = new Connection(channel); // hadoop_rpc是有一个ConnectionManager
            key.attach(connection);
            Reader reader = getReader();
            reader.addConnection(connection);
            LOGGER.info(String.format("接入：%s:%d", connection.getHostAddr(), connection.getPort()));
        }
    }

    @Override
    public void run() {
        LOGGER.info("Listener " + this.getName() + " start...");
        try {
            while (true) {
                SelectionKey key = null;
                try {
                    getSelecor().select();
                    Iterator<SelectionKey> iterator = getSelecor().selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        key = iterator.next();
                        iterator.remove();
                        if (key.isValid() && key.isAcceptable()) {
                            doAccept(key);
                        }
                        key = null;
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            try {
                acceptChannel.close();
                selecor.close();
            } catch (IOException e) {
                // do nothing
            }
            // need to close all connnection.
        }
    }
}
