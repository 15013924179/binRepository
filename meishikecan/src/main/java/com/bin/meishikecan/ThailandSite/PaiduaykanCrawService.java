package com.bin.meishikecan.ThailandSite;

import com.bin.meishikecan.utils.MySeleniumUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

@Slf4j
@Service
public class PaiduaykanCrawService {

    private String url ="https://www.paiduaykan.com/travel/category/travel/paiduaykantravel/page/";

    @Autowired
    private MongoTemplate mongoTemplate;

    public void crawListPage(){
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        //最大页数
        int maxPage = 18;
        //当前页
        int page = 1;
        while (page <= 18) {
            webDriver.get(url+page);
            log.info("爬取第" + page + "页");
            //列表项
            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"col-lg-3 col-md-4 col-xs-6 thumb\"]"));

            for (WebElement webElement : elements) {
                Document document = new Document();

                //url
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
                boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "paiduaykan_travel");
                if (exists) {
                    log.info("该列表项已经爬取过");
                    continue;
                }

                //名称
                Optional.ofNullable(webElement.findElement(By.xpath("./a")))
                        .map(x -> {
                            return x.getAttribute("title");
                        })
                        .map(String::trim)
                        .ifPresent(x -> {
                            document.put("title", x);
                        });


                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                document.put("is_craw", false);
                mongoTemplate.save(document, "paiduaykan_travel");
            }
            page++;
        }
    }

    public void crawDetailPage() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "paiduaykan_travel");
        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(2000);

            //保存内容
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@id=\"boxall\"]/div[2]")))
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
            mongoTemplate.save(document, "paiduaykan_travel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }
}
