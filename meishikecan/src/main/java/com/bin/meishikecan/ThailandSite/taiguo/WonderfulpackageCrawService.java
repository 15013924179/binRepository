package com.bin.meishikecan.ThailandSite.taiguo;

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
public class WonderfulpackageCrawService {
    private String url="https://www.wonderfulpackage.com/product/Thailand/";

    @Autowired
    private MongoTemplate mongoTemplate;

    public void crawListPage() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        webDriver.get(url);
        Thread.sleep(10000);

        List<WebElement> elements = webDriver.findElements(By.xpath("//*[contains(@class,\"product-block\")]"));

        for (WebElement webElement : elements){
            Document document = new Document();
            //保存标题
            Optional.ofNullable(webElement.findElement(By.xpath("./a")))
                    .map(x -> {
                        return x.getAttribute("title");
                    })
                    .map(String::trim)
                    .filter(x -> !x.isEmpty())
                    .ifPresent(x -> {
                        document.put("title", x);
                    });
            //保存url
            Optional.ofNullable(webElement.findElement(By.xpath("./a")))
                    .map(x -> {
                        return x.getAttribute("href");
                    })
                    .filter(x -> !x.isEmpty())
                    .ifPresent(x -> {
                        document.put("url", x);
                    });
            //判断是否爬取过该列表项
            String oldUrl = (String) document.get("url");
            boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "wonderfulpackage_travel");
            if (exists) {
                log.info("已爬取过该列表项，跳过");
                continue;
            }
            document.put("is_craw", false);
            document.put("create_time", LocalDateTime.now());
            document.put("update_time", LocalDateTime.now());
            mongoTemplate.save(document, "wonderfulpackage_travel");
        }
    }

    public void crawDetailPage() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "wonderfulpackage_travel");
        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(2000);

            //保存内容
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"column is-two-thirds\"]")))
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
            mongoTemplate.save(document, "wonderfulpackage_travel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }

}
