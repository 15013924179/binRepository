package com.bin.meishikecan.ThailandSite;

import com.bin.meishikecan.entity.SanookTravel;
import com.bin.meishikecan.utils.MySeleniumUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SanookCrawService {

    @Autowired
    private MongoTemplate mongoTemplate;

    //获取列表页
    public void reptileListPage(String url,String table) throws Exception {

        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        webDriver.get(url);
        Thread.sleep(2000);
        log.info("开始爬取列表页");
        //循环点击下一页
        while (true) {
            try {
                log.info("开始翻页");
                WebElement pageButton = webDriver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div/div/div/div[2]/button"));
                pageButton.click();
                Thread.sleep(1000);
            } catch (Exception e) {
                log.info("翻页结束");
                break;
            }
        }
        //所有列表项
        List<WebElement> list = webDriver.findElements(By.xpath("//*[@id=\"__next\"]/div/div[2]/div/div/div/div[2]/div[4]/div/div"));
        for (WebElement webElement : list) {
            Document document = new Document();

            //保存标题
            Optional.ofNullable(webElement.findElement(By.xpath("./article/div[2]/div/h3/span/a")))
                    .map(x -> {
                        return x.getAttribute("title");
                    })
                    .map(String::trim)
                    .filter(x -> !x.isEmpty())
                    .ifPresent(x -> {
                        document.put("title", x);
                    });

            //保存url
            Optional.ofNullable(webElement.findElement(By.xpath("./article/div[2]/div/h3/span/a")))
                    .map(x -> {
                        return x.getAttribute("href");
                    })
                    .filter(x -> !x.isEmpty())
                    .ifPresent(x -> {
                        document.put("url", x);
                    });


            //判断是否爬取过该列表项
            String oldUrl = (String) document.get("url");
            boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, table);
            if (exists) {
                log.info("已爬取过该列表项，跳过");
                continue;
            }
            document.put("is_craw", false);
            document.put("create_time", LocalDateTime.now());
            document.put("update_time", LocalDateTime.now());
            mongoTemplate.save(document, table);
        }
        log.info("爬取完毕");
        webDriver.close();

    }

    //获取详情页
    public void reptileDetailPage(String table) throws Exception {
        List<Document> list = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, table);
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        for (Document document : list) {
            String url = (String)document.get("url");
            webDriver.get(url);
            Thread.sleep(1000);

            //内容
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,'EntryContent')]/article/div[2]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("content", x);
                        });
            } catch (Exception e) {

            }

            //评论数
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div/div/div[1]/div/div[1]/article/div[1]/div[1]/div/div[1]/div/a/b")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("comment_number", x);
                        });
            } catch (Exception e) {

            }


            document.put("update_time", LocalDateTime.now());
            document.put("is_craw", true);
            mongoTemplate.save(document, table);
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
        webDriver.close();
    }
}
