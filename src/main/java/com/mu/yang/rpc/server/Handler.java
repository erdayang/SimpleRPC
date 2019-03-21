package com.mu.yang.rpc.server;

import com.mu.yang.rpc.chain.JsonDecodeNode;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 真正的调用在这里执行.
 * 1. 解码
 * 2. 找到具体的方法并执行，这里应该是异步的才行.
 * 3. 重新编码并返回
 */
public class Handler extends Thread {
    private static final Logger LOGGER = Logger.getLogger(Handler.class);


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
                process(call);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void process(Call call){
        new JsonDecodeNode().process(call);
    }


}
