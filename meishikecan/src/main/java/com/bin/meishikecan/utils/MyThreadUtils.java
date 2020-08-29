package com.bin.meishikecan.utils;


/**
 * @author wzf
 * @date 2020/7/9 10:52
 * description:
 */
public class MyThreadUtils {


    public static final ThreadLocal<TranslateThreadLocal> THREAD_LOCAL = new ThreadLocal<TranslateThreadLocal>();

    private MyThreadUtils() {
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
