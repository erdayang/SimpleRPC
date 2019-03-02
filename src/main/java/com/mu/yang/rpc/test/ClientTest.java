package com.mu.yang.rpc.test;

import com.mu.yang.rpc.client.Client;
import com.mu.yang.rpc.utils.TimeUtil;

/**
 */
public class ClientTest {
    public static void main(String[] args) {
        Client client = new Client();

        IHelloWorld helloWorld = client.proxyBuilder(IHelloWorld.class)
            .withServer("127.0.0.1")
            .withPort(8080)
            .build();
        long allTtime = 0;
        int count = 10000000;
        for (int i = 0; i < count; i++) {
            long begin = TimeUtil.now();
            System.out.print(i + " + " + (i * 2) + " = ");
            System.out.println(helloWorld.add(i, i * 2));
            long end = TimeUtil.now();
            System.out.println("consume: " + (end - begin));
            allTtime += (end - begin);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("all consume: " + allTtime);
        System.out.println("average consume: " + allTtime / count);
        System.exit(0);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Client.shutdown();
            }
        });
    }
}
