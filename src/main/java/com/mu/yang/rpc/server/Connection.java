package com.mu.yang.rpc.server;

import com.mu.yang.rpc.entity.Request;
import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by yangxianda on 2017/2/25.
 */
public class Connection {
    public SocketChannel channel = null;
    private Socket socket = null;
    private InetAddress addr = null;
    private int port ;
    private String hostAddr = null;
    private ByteBuffer dataBuffer = null;
    private ByteBuffer dataLength = null;
    private ByteBuffer headerBuffer = null;
    private int HEADER_LENGTH = 4;
    public Connection(SocketChannel channel){
        this.channel = channel;
        this.socket = channel.socket();

        this.addr = socket.getInetAddress();
        if(addr==null){
            hostAddr = "UNKNOWN";
        }else{
            hostAddr = addr.getHostAddress();
        }

        this.port = socket.getPort();
        headerBuffer = ByteBuffer.allocate(HEADER_LENGTH);
        System.out.println("create new Connection");
    }

    public Request readAndProcess() throws IOException {
        int count = -1;
        System.out.println("connection read...");

        count = ChannelUtils.channelRead(channel, headerBuffer);
        System.out.println("count = "+ count);
        if(count < 0 || headerBuffer.hasRemaining()){
            return null;
        }
        headerBuffer.flip();
        int length = headerBuffer.getInt();
        System.out.println("data length=" + length);
        headerBuffer = ByteBuffer.allocate(HEADER_LENGTH);

        dataBuffer = ByteBuffer.allocate(length);

        count = ChannelUtils.channelRead(channel, dataBuffer);
        System.out.println("read data count="+count);
        String str = new String(dataBuffer.array());
        System.out.println(str);
        dataBuffer.flip();
        Request request = JSON.parseObject(str, Request.class);
        System.out.println("get Request: " + JSON.toJSONString(request));

        return request;
    }


}
