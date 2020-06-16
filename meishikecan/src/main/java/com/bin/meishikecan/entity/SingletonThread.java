package com.bin.meishikecan.entity;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SingletonThread implements Runnable{

    private static final Lock lock=new ReentrantLock();

    @Override
    public void run() {
        for (int i=0;i<1000000;i++){
            try {
                lock.lock();
                Singleton singleton = Singleton.getTarget();
                singleton.setData(singleton.getData()+1);
                System.out.println(Thread.currentThread().getName()+":"+singleton.getData());
            }catch (Exception e){
                System.out.println(e);
            }finally {
                lock.unlock();
            }


        }

    }
}
