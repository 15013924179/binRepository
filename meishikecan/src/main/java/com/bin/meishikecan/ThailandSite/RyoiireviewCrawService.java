package com.bin.meishikecan.ThailandSite;

import com.bin.meishikecan.utils.MySeleniumUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class RyoiireviewCrawService {
    private static String url = "https://www.ryoiireview.com/recommend-restaurant/";

    @Autowired
    private MongoTemplate mongoTemplate;

    public void crawListPage() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        webDriver.get(url);
        Thread.sleep(2000);
        //当前页
        int pageUrl = 1;
        //最大页数
        int maxPage = Integer.parseInt(webDriver.findElement(By.xpath("//*[@id=\"page-content\"]/div[2]/div[1]/div/div[1]/div[2]/div/div[3]/a[last()-1]")).getText());
        log.info("初始化结束，开始爬取列表页：https://www.ryoiireview.com/recommend-restaurant/");
        while (pageUrl <= maxPage) {
            webDriver.get(url + "?page=" + pageUrl);
            Thread.sleep(2000);
            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"thumbnail\"]"));
            //遍历当前页
            for (int i = 1; i < elements.size(); i++) {
                Document document = new Document();
                WebElement element = elements.get(i);
                //url
                Optional.ofNullable(element.findElement(By.xpath("./div/div[1]/a")))
                        .map(x -> {
                            return x.getAttribute("href");
                        })
                        .ifPresent(x -> {
                            document.put("url", x);
                        });

                //判断是否爬取过该列表项
                String oldUrl = (String) document.get("url");
                boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "ryoiireview_restaurant");
                if (exists) {
                    log.info("该列表项已经爬取过");
                    continue;
                }

                //title
                Optional.ofNullable(element.findElement(By.xpath("./div/div[1]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .ifPresent(x -> {
                            document.put("title", x);
                        });

                document.put("is_craw", false);
                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                mongoTemplate.insert(document, "ryoiireview_restaurant");
            }
            log.info("第" + pageUrl + "页列表项爬取完毕");
            pageUrl++;
        }

        log.info("列表项爬取完毕");
    }

    public void crawDetailPage() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "ryoiireview_restaurant");
        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(2000);

            //保存内容
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@id=\"main-content\"]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("content", x);
                        });
            } catch (Exception e) {

            }

            document.put("update_time", LocalDateTime.now());
            document.put("is_craw", true);
            mongoTemplate.save(document, "ryoiireview_restaurant");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }
}
