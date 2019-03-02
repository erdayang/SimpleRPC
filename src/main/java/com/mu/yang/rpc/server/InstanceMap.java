package com.mu.yang.rpc.server;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于管理需要在服务端服务的实现代码.
 * 可以用注解来实现.
 * 是不是还要采用对象池？
 * 或者
 */
public class InstanceMap {

    /**
     * the delegate objects to
     */
    private static final Map<String, Object> objectMap = new HashMap<String, Object>();


    public static void addClass(Class<?> clazz){
        Class<?>[] objs = clazz.getInterfaces();
        try {
            for(Class<?> inter : objs){
                objectMap.put(inter.getName(), clazz.newInstance());
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Object getInstance(String key){
        return objectMap.get(key);
    }
}
