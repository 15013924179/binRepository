package com.bin.meishikecan.utils;

import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Selenium 工具类
 *
 * @author wangcunlu
 */
@Slf4j
public class MySeleniumUtils {
    private MySeleniumUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static RestTemplate restTemplate = new RestTemplate();

    private static MongoTemplate mongoTemplate;

    static {
        MongoClient mongoClient = new MongoClient("192.168.124.107", 27017);
        mongoTemplate = new MongoTemplate(mongoClient, "dongmeng");
    }

    /**
     * 让webDriver 转换为 HtmlPage 对象
     *
     * @param webDriver webDriver 对象
     * @return HtmlPage 对象
     */
    public static HtmlPage resolveHtmlSourceToPageObject(WebDriver webDriver) {
        HtmlPage page = null;
        if (webDriver != null) {
            WebClient webClient = new WebClient();
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(false);
            String pageUrl = webDriver.getCurrentUrl();
            String source = webDriver.getPageSource();

            URL url = null;
            try {
                url = new URL(pageUrl);
            } catch (MalformedURLException e) {
                log.error("context", e);
            }
            StringWebResponse response = new StringWebResponse(source, url);
            try {
                page = HTMLParser.parseHtml(response, webClient.getCurrentWindow());
            } catch (IOException e) {
                log.error("context", e);
            }
            webClient.close();
        }
        return page;
    }


    /**
     * 获取Driver,不显示图片
     *
     * @return
     */
    public static WebDriver getWebDriver() {
        System.setProperty("webdriver.chrome.driver", "D:\\chromedriver.exe");
        ChromeOptions chromeOptions = new ChromeOptions();
//        chromeOptions.addArguments("blink-settings=imagesEnabled=false");
        chromeOptions.addArguments("--start-maximized");
        // 打开或者关闭浏览器视图
//        chromeOptions.addArguments("--no-sandbox");
//        chromeOptions.addArguments("--disable-dev-shm-usage");
//        chromeOptions.addArguments("--headless");
        return new ChromeDriver(chromeOptions);
    }

    /**
     * 获取Driver,显示图片
     *
     * @return
     */
    public static WebDriver getWebDriverHavingImg() {
        System.setProperty("webdriver.chrome.driver", "D:\\chromedriver.exe");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--start-maximized");
        return new ChromeDriver(chromeOptions);
    }

    /**
     * 获取代理ip的Driver
     */
    public static WebDriver getProxyIpDriver() {
        System.setProperty("webdriver.chrome.driver", "D:\\chromedriver.exe");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--start-maximized");
        chromeOptions.addArguments("blink-settings=imagesEnabled=false");
        String ip = restTemplate.getForObject("http://http.tiqu.alicdns.com/getip3?num=1&type=1&pro=&city=0&yys=0&port=1&time=1&ts=0&ys=0&cs=0&lb=1&sb=0&pb=4&mr=2&regions=&gm=4", String.class);
        chromeOptions.addArguments("--proxy-server=http://" + ip);
        log.info("代理ip：" + ip);
        return new ChromeDriver(chromeOptions);
    }


}
