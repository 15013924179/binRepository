package com.bin.meishikecan.common;

import com.bin.meishikecan.utils.MySeleniumUtils;
import org.openqa.selenium.WebDriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多线程模拟实现生产者／消费者模型
 *
 * @author 林计钦
 * @version 1.0 2013-7-25 下午05:23:11
 */
public class ProducerConsumer {
    /**
     * 定义装苹果的篮子
     */
    public class Basket {
        // 篮子，能够容纳3个苹果
        BlockingQueue<String> basket = new LinkedBlockingQueue<>();

        // 生产苹果，放入篮子
        public void produce() throws InterruptedException {

            //读取m3u8文件
            File file = new File("index.m3u8");
            BufferedReader reader = null;
            StringBuffer sbf = new StringBuffer();
            try {
                reader = new BufferedReader(new FileReader(file));
                String tempStr;
                while ((tempStr = reader.readLine()) != null) {
                    sbf.append(tempStr);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            //正则匹配筛选
            String regex = "\\w+.ts"; //正则表达式
            Pattern pattern = Pattern.compile(regex);
            Matcher m = pattern.matcher(sbf.toString());

            while (m.find()) {
                basket.put("URL" + m.group());
            }


        }

        // 消费苹果，从篮子中取走
        public String consume() throws InterruptedException {
            // take方法取出一个苹果，若basket为空，等到basket有苹果为止(获取并移除此队列的头部)
            return basket.take();
        }
    }

    // 定义苹果生产者
    class Producer implements Runnable {
        private Basket basket;

        public Producer(Basket basket) {
            this.basket = basket;
        }

        public void run() {
            try {
                // 生产苹果
                System.out.println("生产者准备生产数据");
                basket.produce();
                System.out.println("生产者生产数据完毕");
                // 休眠300ms
                Thread.sleep(300);

            } catch (InterruptedException ex) {
                System.out.println("Producer Interrupted");
            }
        }
    }

    // 定义苹果消费者
    class Consumer implements Runnable {
        private String instance;
        private Basket basket;

        public Consumer(String instance, Basket basket) {
            this.instance = instance;
            this.basket = basket;
        }

        public void run() {

            WebDriver webDriver = MySeleniumUtils.getWebDriver();
            try {
                while (true) {
                    // 消费
                    String url = basket.consume();
                    webDriver.get(url);
                    System.out.println(instance+"消费url完毕：" + url);
                }
            } catch (InterruptedException ex) {
                System.out.println("Consumer Interrupted");
            }
        }
    }

    public static void main(String[] args) throws Exception{
        ProducerConsumer test = new ProducerConsumer();

        // 建立一个装苹果的篮子
        Basket basket = test.new Basket();

        ExecutorService service = Executors.newCachedThreadPool();
        Producer producer = test.new Producer(basket);
        service.submit(producer);
        for (int i =0;i<5;i++) {
            Consumer consumer = test.new Consumer("消费者"+i, basket);
            service.submit(consumer);
        }

          Thread.currentThread().join();
          service.shutdownNow();
    }

}
