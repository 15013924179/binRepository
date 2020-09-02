package com.bin.meishikecan.ThailandSite.yuenan;

import com.bin.meishikecan.utils.MySeleniumUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
public class YuenanMytourCrawService {

    @Resource
    private MongoTemplate mongoTemplate;

    public void crawsListPage() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();

        Pattern pattern = Pattern.compile("[0-9|,]+");

        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "yuenan_mytour_hotel_area");

        for (Document area : documents) {
            String title = (String) area.get("title");
            String url = (String) area.get("url");

            log.info("开始爬取：" + title);

            webDriver.get(url);

            Thread.sleep(2000);

            try {
                WebElement element = webDriver.findElement(By.xpath("//*[@id=\"icon-close-button-1583486151716\"]"));

                element.click();

                Thread.sleep(1000);
            }catch (Exception e) {

            }


            Thread.sleep(1000);

            WebElement next;

            try{
                next = webDriver.findElement(By.xpath("//*[text()=\"Trang sau »\"]"));
            }catch (Exception e) {
                next = null;
            }

            int page = 1;

            while (true) {
                log.info("第" + page + "页");

                List<WebElement> elements = webDriver.findElements(By.xpath("//*[@id=\"data-hotels\"]//*[contains(@class,\"product-item\")]"));

                for (WebElement webElement : elements) {
                    Document document = new Document();

                    try {
                        //url
                        Optional.ofNullable(webElement.findElement(By.xpath("./div[1]/h2/a")))
                                .map(x -> {
                                    String href = x.getAttribute("href");
                                    return href;
                                })
                                .filter(x -> !x.isEmpty())
                                .ifPresent(x -> {
                                    document.put("url", x);
                                });
                        //判断是否爬取过该列表项
                        String oldUrl = (String) document.get("url");
                        boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "yuenan_mytour_hotel");
                        if (exists) {
                            log.info("该列表项已经爬取过");
                            continue;
                        }
                    } catch (Exception e) {

                    }

                    try {
                        //名称
                        Optional.ofNullable(webElement.findElement(By.xpath("./div[1]/h2/a")))
                                .map(WebElement::getText)
                                .map(String::trim)
                                .ifPresent(x -> {
                                    document.put("title", x);
                                });
                    } catch (Exception e) {

                    }

                    try {
                        //評分
                        Optional.ofNullable(webElement.findElement(By.xpath(".//*[@class=\"box-review\"]/span[1]")))
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
                        Optional.ofNullable(webElement.findElement(By.xpath(".//*[@class=\"rate-number-info\"]/a")))
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

                    try {
                        //地址
                        Optional.ofNullable(webElement.findElement(By.xpath(".//*[@class=\"gray\"]")))
                                .map(WebElement::getText)
                                .map(String::trim)
                                .ifPresent(x -> {
                                    document.put("address", x);
                                });
                    } catch (Exception e) {
                    }

                    document.put("create_time", LocalDateTime.now());
                    document.put("update_time", LocalDateTime.now());
                    document.put("is_craw", true);
                    document.put("is_translate", false);
                    mongoTemplate.save(document, "yuenan_mytour_hotel");

                }

                try {
                    next.click();
                }catch (Exception e) {
                    break;
                }


                Thread.sleep(4000);

                //下一页按钮
                try{
                    next = webDriver.findElement(By.xpath("//*[text()=\"Trang sau »\"]"));
                }catch (Exception e) {
                    break;
                }

                try{
                    next = webDriver.findElement(By.xpath("//*[@class=\"paginationjs-next disabled\"]"));

                    break;
                }catch (Exception e) {

                }


                page++;

            }

            area.put("is_craw", true);

            mongoTemplate.save(area, "yuenan_mytour_hotel_area");

        }

    }

}
