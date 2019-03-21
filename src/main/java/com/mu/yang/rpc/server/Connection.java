package com.mu.yang.rpc.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Listener对于一次channel的接入，创建一个connection.
 */
public class Connection {

    public SocketChannel channel = null;
    private Socket socket = null;
    private InetAddress addr = null;
    private int port;
    private String hostAddr = null;
    private ByteBuffer dataBuffer = null;
    private ByteBuffer protocolBuffer = null;
    private ByteBuffer headerBuffer = null;
    private int HEADER_LENGTH = 4;

    public Connection(SocketChannel channel) {
        this.channel = channel;
        this.socket = channel.socket();

        this.addr = socket.getInetAddress();
        if (addr == null) {
            hostAddr = "UNKNOWN";
        } else {
            hostAddr = addr.getHostAddress();
        }

        this.port = socket.getPort();
        protocolBuffer = ByteBuffer.allocate(4);
        headerBuffer = ByteBuffer.allocate(HEADER_LENGTH);
        System.out.println("create new Connection");
    }

    public byte[] read() throws IOException {
        int count = -1;
        System.out.println("connection read...");


        count = ChannelUtils.channelRead(channel, protocolBuffer);
        System.out.println("count = " + count);
        if (count < 0 || protocolBuffer.hasRemaining()) {
            return null;
        }
        protocolBuffer.flip();

        count = ChannelUtils.channelRead(channel, headerBuffer);
        System.out.println("count = " + count);
        if (count < 0 || headerBuffer.hasRemaining()) {
            return null;
        }
        headerBuffer.flip();
        int length = headerBuffer.getInt();
        System.out.println("data length=" + length);
        headerBuffer = ByteBuffer.allocate(HEADER_LENGTH);

        dataBuffer = ByteBuffer.allocate(length);

        count = ChannelUtils.channelRead(channel, dataBuffer);
        System.out.println("read data count=" + count);
        dataBuffer.flip();
        return dataBuffer.array();
    }


    public InetAddress getAddr() {
        return addr;
    }

    public void setAddr(InetAddress addr) {
        this.addr = addr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHostAddr() {
        return hostAddr;
    }

    public void setHostAddr(String hostAddr) {
        this.hostAddr = hostAddr;
    }
}
