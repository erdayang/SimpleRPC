package com.mu.yang.rpc.chain;

import com.alibaba.fastjson.JSON;
import com.mu.yang.rpc.entity.JsonRequest;
import com.mu.yang.rpc.entity.Response;
import com.mu.yang.rpc.entity.ResultCode;
import com.mu.yang.rpc.server.Call;
import com.mu.yang.rpc.server.ChannelUtils;
import com.mu.yang.rpc.server.InstanceMap;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * 主要用于处理json格式.
 */
public class JsonDecodeNode implements Node {
    private static final Logger LOGGER = Logger.getLogger(JsonDecodeNode.class);

    @Override
    public void process(Call call) {
        String str = new String(call.getData());
        JsonRequest request = JSON.parseObject(str, JsonRequest.class);
        Response response = processRequest(request);
        call.setResponse(response);
        //  responseQueue.add(call);
        byte[] aa = call.getResponse().toString().getBytes();
        System.out.println(aa.length);
        ByteBuffer buffer = ByteBuffer.allocate(4 + aa.length);
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

    public Response processRequest(final JsonRequest request) {
        Response response = new Response();
        response.setId(request.getId());
        try {
            Object obj = InstanceMap.getInstance(request.getClazz());
            Class clazz = obj.getClass();
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
