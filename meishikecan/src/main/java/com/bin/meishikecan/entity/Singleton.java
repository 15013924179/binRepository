package com.bin.meishikecan.entity;

import lombok.Data;

/**
 * 单例模式测试用例
 *
 */

@Data
public class Singleton {

    private volatile Integer data=0;  //成员变量

    private Singleton(){}   //构造函数私有化

    //静态内部类
    private static class Target{
        private static final Singleton singleton = new Singleton();//内部类创建实例
    }

    //当任何一个线程第一次调用getTarget()时，都会使Target被
    //加载和被初始化，此时静态初始化器将执行Singleton的初始化操作。
    // (被调用时才进行初始化！)初始化静态数据时，Java提供了的线程安全性保证。
    public static final Singleton getTarget(){
        return Target.singleton;
    }


}
