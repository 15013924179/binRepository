package com.bin.meishikecan.ThailandSite.yuenan;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class YuenanBookingCrawsService {
    @Autowired
    private MongoTemplate mongoTemplate;

    public void crawsListPage() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();

        Pattern pattern = Pattern.compile("[0-9|,]+");

        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "yuenan_area");

        for (Document area : documents) {
            String title = (String) area.get("title");
            String url = (String) area.get("url");

            log.info("开始爬取：" + title);

            webDriver.get(url);

            WebElement next;

            try{
                next = webDriver.findElement(By.xpath("//*[@title=\"Next page\"]"));
            }catch (Exception e) {
                next = null;
            }

            int page = 1;

            while (true) {
                log.info("第" + page + "页");

                List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"sr_item_content sr_item_content_slider_wrapper \"]"));

                for (WebElement webElement : elements) {
                    Document document = new Document();

                    try {
                        //url
                        Optional.ofNullable(webElement.findElement(By.xpath("./div/div/div/h3/a")))
                                .map(x -> {
                                    String href = x.getAttribute("href");
                                    href = href.split("\\?")[0];
                                    return href;
                                })
                                .filter(x -> !x.isEmpty())
                                .ifPresent(x -> {
                                    document.put("url", x);
                                });
                        //判断是否爬取过该列表项
                        String oldUrl = (String) document.get("url");
                        boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "yuenan_booking_hotel");
                        if (exists) {
                            log.info("该列表项已经爬取过");
                            continue;
                        }
                    } catch (Exception e) {

                    }

                    try {
                        //名称
                        Optional.ofNullable(webElement.findElement(By.xpath("./div/div/div/h3/a/span")))
                                .map(WebElement::getText)
                                .map(String::trim)
                                .ifPresent(x -> {
                                    document.put("title", x);
                                });
                    } catch (Exception e) {

                    }

                    try {
                        //評分
                        Optional.ofNullable(webElement.findElement(By.xpath("./div/div[2]/div/div[1]/a//*[@class=\"bui-review-score__badge\"]")))
                                .map(WebElement::getText)
                                .map(String::trim)
                                .ifPresent(x -> {
                                    document.put("score", x);
                                });
                    } catch (Exception e) {
                        document.put("score", "0");
                    }

                    try {
                        //評分数量
                        Optional.ofNullable(webElement.findElement(By.xpath("./div/div[2]/div/div[1]/a//*[@class=\"bui-review-score__text\"]")))
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
                                .ifPresent(x -> {
                                    document.put("score_number", x);
                                });
                    } catch (Exception e) {
                        document.put("score_number", "0");
                    }

                    document.put("create_time", LocalDateTime.now());
                    document.put("update_time", LocalDateTime.now());
                    document.put("is_craw", false);
                    document.put("is_translate", false);
                    mongoTemplate.save(document, "yuenan_booking_hotel");

                }

                try {
                    next.click();
                }catch (Exception e) {
                    break;
                }


                Thread.sleep(4000);

                //下一页按钮
                next = webDriver.findElement(By.xpath("//*[@title=\"Next page\"]"));

                try {
                    webDriver.findElement(By.xpath("//*[contains(@class,\"bui-pagination__item bui-pagination__next-arrow bui-pagination__item--disabled\")]"));
                    break;
                } catch (Exception e) {
                }
                page++;

            }

            area.put("is_craw", true);

            mongoTemplate.save(area, "yuenan_area");

        }

    }

    public void crawDetailPage () throws Exception{
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "yuenan_booking_hotel");

        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(4000);

            //地址
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@data-component= \"tooltip\"]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("address", x);
                        });
            } catch (Exception e) {

            }


            document.put("update_time", LocalDateTime.now());
            document.put("is_craw", true);
            mongoTemplate.save(document, "yuenan_booking_hotel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }

}
