package com.mu.yang.rpc.server;

import com.alibaba.fastjson.JSON;
import com.mu.yang.rpc.entity.Request;
import com.mu.yang.rpc.entity.Response;
import com.mu.yang.rpc.entity.ResultCode;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;

/**
 * 真正的调用在这里执行.
 * 1. 解码
 * 2. 找到具体的方法并执行，这里应该是异步的才行.
 * 3. 重新编码并返回
 */
public class Handler extends Thread {
    public static final Logger LOGGER = Logger.getLogger(Handler.class);


    private BlockingQueue<Call> requestQueue;
    private BlockingQueue<Call> responseQueue;
//    private Writer writer;
    public Handler(String name, BlockingQueue<Call> requestQueue){
        super.setName(name);
        this.requestQueue = requestQueue;
        this.responseQueue = new LinkedBlockingQueue<Call>();
    }

    public void run(){
        LOGGER.debug(this.getName() + "start...");
        while(true){
            try {
                final Call call = requestQueue.take();
                LOGGER.debug(this.getName() + " process call:" + call.getRequest().getId());
                process(call);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void process(Call call){
        Request request = call.getRequest();
        Response response = processRequest(request);
        call.setResponse(response);
      //  responseQueue.add(call);
        byte[] aa = call.getResponse().toString().getBytes();
        System.out.println(aa.length);
        ByteBuffer buffer = ByteBuffer.allocate(4+aa.length);
        buffer.putInt(aa.length);
        buffer.put(aa);
        buffer.flip();
        try {
            int num = ChannelUtils.channelWrite(call.getConnection().channel, buffer);
            LOGGER.debug(num);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("response id=" + request.getId());
    //    writer.wakeUp();
    }

    public Response processRequest(final Request request){
        Response response = new Response();
        response.setId(request.getId());
        try {
            Object obj = InstanceMap.getInstance(request.getClazz());
            Class clazz= obj.getClass();
            LOGGER.error(JSON.toJSONString(clazz.getMethods()));
            Method method = clazz.getMethod(request.getMethod(), request.getParamType());
            Object result = method.invoke(obj, request.getParams());
            response.setResult(result);
            response.setCode(ResultCode.SUCCESS);
        } catch (IllegalAccessException e) {
            LOGGER.error(e);
            response.setCode(ResultCode.NO_SUCH_METHOD);
            response.setError(e);
        } catch (NoSuchMethodException e) {
            LOGGER.error(e);
            response.setCode(ResultCode.NO_SUCH_METHOD);
            response.setError(e);
        } catch (InvocationTargetException e) {
            LOGGER.error(e);
        }
        LOGGER.debug("create response done");
        return response;
    }

}
