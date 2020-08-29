package com.bin.meishikecan.ThailandSite.malaixiya;

import com.bin.meishikecan.utils.MySeleniumUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class MalaixiyaQyerCrawsService {

    @Resource
    private MongoTemplate mongoTemplate;

    public void crawListPageTravel() throws Exception {

        WebDriver webDriver = MySeleniumUtils.getWebDriver();

        webDriver.get("https://place.qyer.com/malaysia/sight/");

        Actions actions = new Actions(webDriver);

        int page = 1;

        WebElement next = webDriver.findElement(By.xpath("//*[@title=\"下一页\"]"));

        while (next != null) {

            log.info("第"+page+"页");

            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@id=\"poiLists\"]/li//*[@class=\"title fontYaHei\"]/a"));

            for (WebElement webElement : elements) {
                Document document = new Document();

                try {
                    //url
                    Optional.ofNullable(webElement)
                            .map(x -> {
                                return x.getAttribute("href");
                            })
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("url", x);
                            });
                    //判断是否爬取过该列表项
                    String oldUrl = (String) document.get("url");
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "malaixiya_qyer_travel");
                    if (exists) {
                        log.info("该列表项已经爬取过");
                        continue;
                    }
                } catch (Exception e) {

                }

                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                document.put("is_craw", false);
                mongoTemplate.save(document, "malaixiya_qyer_travel");

            }

            webDriver.findElement(By.xpath("/html[1]")).sendKeys(Keys.END);

            actions.moveToElement(next).click().perform();

            Thread.sleep(3000);

            try {
                next = webDriver.findElement(By.xpath("//*[@title=\"下一页\"]"));
            }catch (Exception e) {
                next = null;
            }

            page++;

        }


    }

    public void crawDetailPageTravel() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        Pattern pattern = Pattern.compile("[0-9,]+");
        log.info("旅店开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "malaixiya_qyer_travel");
        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(2000);

            //地址
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"poi-tips\"]//*[contains(text(),\"地址：\")]/..//p")))
                        .map(WebElement::getText)
                        .map(x -> {
                            return x.replace("(查看地图)", "");
                        })
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("address", x);
                        });
            } catch (Exception e) {

            }


            //保存联系
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"poi-tips\"]//*[contains(text(),\"电话：\")]/..//p")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("contact", x);
                        });
            } catch (Exception e) {

            }

            //保存交通
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"poi-tips\"]//*[contains(text(),\"到达方式：\")]/..//p")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("traffic", x);
                        });
            } catch (Exception e) {

            }

            //保存开放时间
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"poi-tips\"]//*[contains(text(),\"开放时间：\")]/..//p")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("open_time", x);
                        });
            } catch (Exception e) {

            }

            //保存门票价格
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"poi-tips\"]//*[contains(text(),\"门票：\")]/..//p")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("price", x);
                        });
            } catch (Exception e) {

            }

            //保存网址
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"poi-tips\"]//*[contains(text(),\"网址：\")]/..//p")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("website", x);
                        });
            } catch (Exception e) {

            }



            //评分
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"poi-placeinfo clearfix\"]//*[@class=\"points\"]/span[1]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("score", x);
                        });
            } catch (Exception e) {
                document.put("score", "0");
            }


            //评论数
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"poi-placeinfo clearfix\"]//*[@class=\"summery\"]")))
                        .map(WebElement::getText)
                        .map(x -> {
                            //正则匹配
                            Matcher matcher = pattern.matcher(x);
                            if (matcher.find()) {
                                return matcher.group();
                            }
                            return "0";
                        })
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("score_number", x);
                        });
            } catch (Exception e) {
                document.put("score_number", "0");
            }

            //排名
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"poi-placeinfo clearfix\"]//*[@class=\"rank\"]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("rank", x);
                        });
            } catch (Exception e) {

            }


            document.put("update_time", LocalDateTime.now());
            document.put("is_craw", true);
            mongoTemplate.save(document, "malaixiya_qyer_travel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }

}
