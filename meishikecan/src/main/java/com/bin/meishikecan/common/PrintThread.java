package com.bin.meishikecan.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 等待唤醒机制：有三个线程ID分别是A、B、C,请有多线编程实现，在屏幕上循环打印10次ABCABC
 */
public class PrintThread implements Runnable {
    private int count = 0;

    @Override
    public void run() {
        String name = Thread.currentThread().getName();
        synchronized (this) {
            while (count < 30) {
                if ((count % 3) == 0) {
                    if (name.equals("A")) {
                        this.notifyAll();
                        System.out.print(name);
                        count++;
                    } else {
                        try {
                            this.wait();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                if ((count % 3) == 1) {
                    if (name.equals("B")) {
                        this.notifyAll();
                        System.out.print(name);
                        count++;
                    } else {
                        try {
                            this.wait();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
                if ((count % 3) == 2) {
                    if (name.equals("C")) {
                        this.notifyAll();
                        System.out.print(name);
                        count++;
                    } else {
                        try {
                            this.wait();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }

    }


    public static void main(String[] args) {
        PrintThread printThread = new PrintThread();
        Thread thread1 = new Thread(printThread, "A");
        Thread thread2 = new Thread(printThread, "B");
        Thread thread3 = new Thread(printThread, "C");
        thread1.start();
        thread2.start();
        thread3.start();


    }
}
