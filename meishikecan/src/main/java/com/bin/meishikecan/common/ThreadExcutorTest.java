package com.bin.meishikecan.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadExcutorTest {



    public static void main(String[] args) {
        /**
         * 3种线程池的测试
         */
        ExecutorService executorService = Executors.newCachedThreadPool();
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        ExecutorService executorService = Executors.newFixedThreadPool(10);
        while (true) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + " is running ..");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
          //延期线程池的测试
//        ScheduledExecutorService scheduledThreadPool= Executors.newScheduledThreadPool(3);
//        scheduledThreadPool.schedule(new Runnable(){
//            @Override
//            public void run() {
//                System.out.println("延迟三秒");
//            }
//        }, 3, TimeUnit.SECONDS);
//        scheduledThreadPool.scheduleAtFixedRate(new Runnable(){
//            @Override
//            public void run() {
//                System.out.println("延迟 1 秒后每三秒执行一次");
//            }
//        },1,3,TimeUnit.SECONDS);
    }
}
