package com.bin.meishikecan.utils;

import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Selenium 工具类
 * @author wangcunlu
 */
@Slf4j
public class MySeleniumUtils {
    private MySeleniumUtils(){
        throw new IllegalStateException("Utility class");
    }
    /**
     * 让webDriver 转换为 HtmlPage 对象
     * @param webDriver webDriver 对象
     * @return HtmlPage 对象
     */
    public static HtmlPage resolveHtmlSourceToPageObject(WebDriver webDriver){
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
                log.error("context",e);
            }
            StringWebResponse response = new StringWebResponse(source, url);
            try {
                page = HTMLParser.parseHtml(response, webClient.getCurrentWindow());
            } catch (IOException e) {
                log.error("context",e);
            }
            webClient.close();
        }
        return page;
    }


    /**
     * 获取Driver,不显示图片
     * @return
     */
    public static WebDriver getWebDriver() {
        // 配置浏览器
        WebClient webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        System.setProperty("webdriver.chrome.driver", "D:\\chromedriver.exe");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("blink-settings=imagesEnabled=false");
        chromeOptions.addArguments("--start-maximized");
        // 打开或者关闭浏览器视图
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        chromeOptions.addArguments("--headless");
        return new ChromeDriver(chromeOptions);
    }

    /**
     * 获取Driver,显示图片
     * @return
     */
    public static WebDriver getWebDriverHavingImg() {
        // 配置浏览器
        WebClient webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        System.setProperty("webdriver.chrome.driver", "D:\\chromedriver.exe");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--start-maximized");
        return new ChromeDriver(chromeOptions);
    }

}
