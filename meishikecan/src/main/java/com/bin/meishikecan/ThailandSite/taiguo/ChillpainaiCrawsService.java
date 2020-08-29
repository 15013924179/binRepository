package com.bin.meishikecan.ThailandSite.taiguo;

import com.bin.meishikecan.utils.MySeleniumUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ChillpainaiCrawsService {
    private static String url = "https://www.chillpainai.com/";

    @Autowired
    private MongoTemplate mongoTemplate;

    public void crawListPage() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        webDriver.get(url);
        Thread.sleep(2000);

        List<WebElement> places = webDriver.findElements(By.xpath("//*[@class=\"modal-content\"]//li/a"));
        List<String> urls = new ArrayList<>();
        for (WebElement webElement : places) {
            String urlPlace = webElement.getAttribute("href");
            urls.add(urlPlace);
        }
        log.info("初始化完毕，开始爬取列表页");

        //爬取每个地点
        for (String urlPlace : urls) {
            webDriver.get(urlPlace);
            Thread.sleep(2000);
            log.info("开始爬取："+urlPlace);
            int i = 1;
            while (true) {
                try {
                    log.info("第" + i + "页开始爬取");
                    List<WebElement> travels = webDriver.findElements(By.xpath("//*[@class=\"box\"]"));
                    for (WebElement webElement : travels){
                        Document document = new Document();
                        //保存标题
                        Optional.ofNullable(webElement.findElement(By.xpath("./div/h4")))
                                .map(WebElement::getText)
                                .map(String::trim)
                                .filter(x -> !x.isEmpty())
                                .ifPresent(x -> {
                                    document.put("title", x);
                                });
                        //保存url
                        Optional.ofNullable(webElement.findElement(By.xpath("./figure/a")))
                                .map(x -> {
                                    return x.getAttribute("href");
                                })
                                .filter(x -> !x.isEmpty())
                                .ifPresent(x -> {
                                    document.put("url", x);
                                });
                        //判断是否爬取过该列表项
                        String oldUrl = (String) document.get("url");
                        boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "chillpainai_travel");
                        if (exists) {
                            log.info("已爬取过该列表项，跳过");
                            continue;
                        }
                        document.put("is_craw", false);
                        document.put("create_time", LocalDateTime.now());
                        document.put("update_time", LocalDateTime.now());
                        mongoTemplate.save(document, "chillpainai_travel");
                    }

                    //获取下一页
                    WebElement button = webDriver.findElement(By.xpath("//*[@class='naviPN'][text()=\"Next\"]"));
                    ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView();", button);
                    for (int k = 0; k < 10; k++) {
                        button.sendKeys(Keys.UP);
                    }
                    Thread.sleep(1000);
                    button.click();
                    Thread.sleep(2000);
                    i++;
                } catch (Exception e) {
                    log.info("没有下一页了");
                    break;
                }

            }
        }
        log.info("爬取列表页完毕");

    }

    public void crawDetailPage() throws Exception{
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "chillpainai_travel");
        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(2000);

            //保存内容
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"post-content\")]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("content", x);
                        });
            } catch (Exception e) {

            }

            //浏览量
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"view\")]/em")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("brower_number", x);
                        });
            } catch (Exception e) {
                document.put("brower_number", "0");
            }

            //转推量
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@id,\"tw-count\")]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("retweet_number", x);
                        });
            } catch (Exception e) {
                document.put("retweet_number", "0");
            }

            //分享量
            try {
                webDriver.switchTo().frame(webDriver.findElement(By.xpath("//*[@id=\"share-btn\"]/div[2]/iframe")));
                Optional.ofNullable(webDriver.findElement(By.xpath("//span[contains(@id,\"u_0_0\")]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("share_number", x);
                        });
            } catch (Exception e) {
                document.put("share_number", "0");
            }

            document.put("update_time", LocalDateTime.now());
            document.put("is_craw", true);
            mongoTemplate.save(document, "chillpainai_travel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }
}
