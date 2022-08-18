package com.atguigu.gmall.cart.utils;

/**
 * @author: rlk
 * @date: 2022/8/9
 * Description:
 */
public class ThreadLocalUtil {

    /**
     * 定义一个全局的ThreadLocal -- 相当于常量，不可以被修改
     * 泛型是String类型的，因为要保存的数据是username
     */
    private final static ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 构造方法私有化
     */
    private ThreadLocalUtil(){}

    /**
     * 取username
     * @return
     */
    public static String get(){
        return THREAD_LOCAL.get();
    }

    public static void set(String username){
        THREAD_LOCAL.set(username);
    }
}
