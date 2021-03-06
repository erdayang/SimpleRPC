package com.mu.yang.rpc.server;

import com.alibaba.fastjson.JSON;
import com.mu.yang.rpc.entity.Request;
import com.mu.yang.rpc.entity.Response;
import com.mu.yang.rpc.entity.ResultCode;
import com.mu.yang.rpc.test.HelloWorld;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangxianda on 2016/12/18.
 */
public class SimpleServer {

    private ServerSocket serverSocket = null;
    private int port = 8080;

    /**
     * the delegate objects to
     */
    private Map<String, Object> objectMap = new HashMap<String, Object>();
    public SimpleServer(int port){
        this.port = port;
        start();
    }

    public void start(){
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                System.out.println("get new Connection: from: " + socket.getInetAddress() +" : " + socket.getPort());
                new Thread(new Handler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown(){

    }

    class Handler implements Runnable{

        Socket socket = null;
        InputStream inputStream;
        OutputStream outputStream;
        Handler(Socket socket){
            this.socket = socket;
            init();
        }
        void init(){
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        public void run() {
            while(!Thread.interrupted()){
                process();
            }
        }

        public void process(){
            try {
                byte[] requestBytes = new byte[4096];
                int size = 0;
                size = inputStream.read(requestBytes);

                String requestString = new String(requestBytes);

                System.out.println("get request： " + requestString);

                Request request = JSON.parseObject(requestString, Request.class);

                Response response = processRequest(request);
                OutputStream outputStream = socket.getOutputStream();

                outputStream.write(response.toString().getBytes());
                outputStream.flush();
                System.out.println("send response: "+ response);
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                Thread.currentThread().interrupt();
            }
        }

        public Response processRequest(Request request){
            Response response = new Response();
            response.setId(request.getId());
            try {
                Class clazz = Class.forName(request.getClazz());
                Object obj = InstanceMap.getInstance(request.getClazz());

                Method method = clazz.getMethod(request.getMethod(), request.getParamType());
                Object result = method.invoke(obj, request.getParams());
                response.setResult(result);
                response.setCode(ResultCode.SUCCESS);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                response.setCode(ResultCode.NOSUCHCLASS);
                response.setError(e);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                response.setCode(ResultCode.NO_SUCH_METHOD);
                response.setError(e);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                response.setCode(ResultCode.NO_SUCH_METHOD);
                response.setError(e);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return response;
        }

    }
}
