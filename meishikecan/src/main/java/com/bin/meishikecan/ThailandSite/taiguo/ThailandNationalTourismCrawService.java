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
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ThailandNationalTourismCrawService {
    private static String url = "https://thai.tourismthailand.org/Search-result/attraction?sort_by=datetime_updated_desc&page=1&perpage=15&menu=attraction";
    @Autowired
    private MongoTemplate mongoTemplate;


    public void crawListPage() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        webDriver.get(url);
        Thread.sleep(2000);
        //获取当前页标签集合
        List<WebElement> pageList = webDriver.findElements(By.xpath("//*[contains(@class,\"page-link\")]"));
        if (pageList == null || pageList.size() < 2) {
            log.error("未查询到页标签或页标签有误");
            return;
        }
        //下一页按钮
        WebElement button = webDriver.findElement(By.xpath("//*[contains(@class,\"pagination\")]/li[last()-1]/a"));
        //获取最大页数
        int maxPage = Integer.parseInt(pageList.get(pageList.size() - 3).getText());
        //当前页数
        log.info("初始化完毕，开始爬取列表页");
        int page = 1;
        while (true) {

            //获取当前页列表项
            List<WebElement> data = webDriver.findElements(By.xpath("//*[contains(@class,\"wrap-default-cards-articles\")]"));

            for (WebElement webElement : data) {
                Document document = new Document();
                //保存标题
                Optional.ofNullable(webElement.findElement(By.xpath("./a/div/div[2]/div/div[1]")))
                        .map(WebElement::getText)
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
                boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "thailand_national_tourism");
                if (exists) {
                    log.info("已爬取过该列表项，跳过");
                    continue;
                }
                document.put("is_craw", false);
                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                mongoTemplate.save(document, "tourismthailand_travel");
            }

            if (maxPage <= page) {
                break;
            }
            log.info("当前第" + page + "页列表爬取完毕,跳转下一页");
            ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView();", button);

            for (int i = 0; i < 10; i++) {
                button.sendKeys(Keys.UP);
            }
            Thread.sleep(1000);
            //点击下一页
            button.click();
            Thread.sleep(1000);
            button = webDriver.findElement(By.xpath("//*[contains(@class,\"pagination\")]/li[last()-1]/a"));
            page++;
        }
        log.info("列表页爬取完毕");

    }


    public void crawDetailPage() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "tourismthailand_travel");
        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(2000);

            //保存类别
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"is-primary font-weight-bold\")]/span")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("category", x);
                        });
            } catch (Exception e) {

            }

            //保存联系
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"contact-box\")]/div")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("contact", x);
                        });
            } catch (Exception e) {

            }

            //保存价格
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(text(),'ค่าธรรมเนียมเข้าชม')]/../div")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("total", x);
                        });
            } catch (Exception e) {

            }

            //保存地点
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[text()='ที่ตั้ง']/../div")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("place", x);
                        });
            } catch (Exception e) {

            }

            //保存内容
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@id=\"interesting_facts\"]/div/div[2]/div/div/div")))
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
            mongoTemplate.save(document, "tourismthailand_travel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }


}
