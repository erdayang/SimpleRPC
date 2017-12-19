package com.mu.yang.rpc.core;

import com.mu.yang.rpc.connector.ConnectorFactory;
import com.mu.yang.rpc.connector.DefaultConnectorFactory;
import com.mu.yang.rpc.proxy.Invoker;
import com.mu.yang.rpc.test.IHelloWorld;
import com.mu.yang.rpc.utils.TimeUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by yangxianda on 2016/12/18.
 */
public class Client {

    private static ConnectorFactory factory ;
    public Client(){

    }

    public <T> ProxyBuilder<T> proxyBuilder(Class<T> clazz){
        return new ProxyBuilder<T>(clazz);
    }


    public class ProxyBuilder<T>{
        private Class<T> clazz;
        private InvocationHandler invocation;
        private boolean isServiceDiscovery;
        private String zookeeper;
        private String path;
        private InetAddress server;
        private int port;
        private String encodeType;
        private boolean sdSwitch = false;
        public ProxyBuilder(Class<T> clazz){
            this.clazz = clazz;
        }

        public ProxyBuilder<T> withServer(String server) {
            try {
                this.server = InetAddress.getByName(server);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            return this;
        }
        public ProxyBuilder<T> withInterface(Class<T> clazz){
            this.clazz = clazz;
            return this;
        }

        public ProxyBuilder<T> withServiceDiscovery(boolean isServiceDiscovery){
            this.isServiceDiscovery = isServiceDiscovery;
            return this;
        }

        public ProxyBuilder<T> withZookeeper(String zookeeper){
            this.zookeeper = zookeeper;
            return this;
        }

        public ProxyBuilder<T> withPath(String path){
            this.path = path;
            return this;
        }

        public ProxyBuilder<T> withPort(int port){
            this.port = port;
            return this;
        }

        public ProxyBuilder<T> withServiceDiscovery(String ips){
            return this;
        }

        public ProxyBuilder<T> withNameSpace(String ns){
            return this;
        }

        private ProxyBuilder<T> withEncodeType(String type){
            this.encodeType = type;
            return this;
        }

        public T build(){
            factory = new DefaultConnectorFactory(server, port);
            invocation = new Invoker(factory);
            T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, invocation);
            return t;
        }
    }

    public static void shutdown(){
        factory.shutdown();
    }

    public static void main(String [] args){
        Client client = new Client();

        IHelloWorld helloWorld = client.proxyBuilder(IHelloWorld.class)
                .withServer("127.0.0.1")
                .withPort(8080)
                .build();
        long allTtime = 0;
        int count = 10;
        for(int i = 0; i < count; i ++){
            long begin = TimeUtil.now();
            System.out.println(helloWorld.get("here"));
          //  System.out.println(helloWorld.get());
            long end = TimeUtil.now() ;
            System.out.println("consume: " + (end - begin));
            allTtime+=(end - begin);
        }
        System.out.println("all consume: " + allTtime);
        System.out.println("average consume: " + allTtime/count);
        System.exit(0);
        Runtime.getRuntime().addShutdownHook(new Thread(Client::shutdown));
    }
}
