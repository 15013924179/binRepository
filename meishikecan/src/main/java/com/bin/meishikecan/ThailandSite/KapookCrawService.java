package com.bin.meishikecan.ThailandSite;

import com.bin.meishikecan.utils.MySeleniumUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class KapookCrawService {

    @Autowired
    private MongoTemplate mongoTemplate;

    //获取列表页
    public void reptileListPage(String url, String table) throws Exception {

        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        webDriver.get(url);
        Thread.sleep(2000);
        log.info("开始爬取列表页");
        //循环点击下一页
        for (int i = 0; i < 1000; i++) {
            try {
                log.info("开始翻页");
                WebElement pageButton = webDriver.findElement(By.xpath("//*[@id=\"loadmore\"]"));
                pageButton.click();
                Thread.sleep(1000);
                int size = webDriver.findElements(By.xpath("//*[@id=\"content\"]/li/a")).size();
                log.info(size + "");
            } catch (Exception e) {
                log.info("翻页结束");
                break;
            }
        }
        //所有列表项
        List<WebElement> list = webDriver.findElements(By.xpath("//*[@id=\"content\"]/li/a"));
        for (WebElement webElement : list) {
            Document document = new Document();

            //保存标题
            Optional.ofNullable(webElement.findElement(By.xpath("./h3")))
                    .map(x -> {
                        return x.getAttribute("title");
                    })
                    .map(String::trim)
                    .filter(x -> !x.isEmpty())
                    .ifPresent(x -> {
                        document.put("title", x);
                    });

            //保存url
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

    }

    //获取详情页
    public void reptileDetailPage(String table) throws Exception {
        List<Document> list = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, table);
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        Pattern pattern = Pattern.compile("[0-9 |,]+");
        for (Document document : list) {
            String url = (String) document.get("url");
            webDriver.get(url);
            Thread.sleep(1000);



            //保存内容
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@id=\"main_article\"]")))
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
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"social-share\"]/p")))
                        .map(WebElement::getText)
                        .map(x -> {
                            return x.replace("อ่าน","");
                        })
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("brower_number", x);
                        });
            } catch (Exception e) {
                try {
                    Optional.ofNullable(webDriver.findElement(By.xpath("//*[@id=\"container\"]/div[1]/div/span[2]")))
                            .map(WebElement::getText)
                            .map(x -> {
                                return x.replace("อ่าน","");
                            })
                            .map(String::trim)
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("brower_number", x);
                            });

                }catch (Exception e1){
                    document.put("brower_number", "0");
                }

            }

            //分享量
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"share-group\"]/div[2]/a/span")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("share_number", x);
                        });
            } catch (Exception e) {
                document.put("share_number", "0");
            }

            //评论数
            try {
                webDriver.switchTo().frame(webDriver.findElement(By.xpath("//*[@id=\"fb-widget\"]/div[2]/span/iframe")));
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"_491z clearfix\"]/div[1]")))
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
                            document.put("comment_number", x);
                        });
            } catch (Exception e) {
                document.put("comment_number", "0");
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
