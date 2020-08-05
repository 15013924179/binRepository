package com.bin.meishikecan.ThailandSite.xinjiapo;

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
public class BookingCrawsService {

    @Autowired
    private MongoTemplate mongoTemplate;

    private String url = "https://www.booking.com/searchresults.zh-cn.html?label=gen173nr-1FCAEoggI46AdIM1gEaGKIAQGYASu4ARfIAQzYAQHoAQH4AQuIAgGoAgO4AuzoovkFwAIB0gIkYWQ2MmI5ZDctOGE1Ny00YzkyLWI0MjUtOWM4ZDkyYjE4NzEx2AIG4AIB&sid=3e507b59d1aeefb4e72d9dc447e56916&sb=1&sb_lp=1&src=index&src_elem=sb&error_url=https%3A%2F%2Fwww.booking.com%2Findex.zh-cn.html%3Flabel%3Dgen173nr-1FCAEoggI46AdIM1gEaGKIAQGYASu4ARfIAQzYAQHoAQH4AQuIAgGoAgO4AuzoovkFwAIB0gIkYWQ2MmI5ZDctOGE1Ny00YzkyLWI0MjUtOWM4ZDkyYjE4NzEx2AIG4AIB%3Bsid%3D3e507b59d1aeefb4e72d9dc447e56916%3Bsb_price_type%3Dtotal%26%3B&ss=%E6%96%B0%E5%8A%A0%E5%9D%A1&is_ski_area=0&checkin_year=&checkin_month=&checkout_year=&checkout_month=&group_adults=2&group_children=0&no_rooms=1&b_h4u_keep_filters=&from_sf=1&ss_raw=xin&dest_id=&dest_type=&search_pageview_id=6d1e07b6caa50054&search_selected=false";

    public void crawsListPage() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();

        webDriver.get(url);

        Thread.sleep(3000);

        Integer maxPage = 23;

        Integer page = 1;

        WebElement next = webDriver.findElement(By.xpath("//*[@title = \"下一页\"]"));

        while (true) {
            log.info("第" + page + "页");

            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class= \"sr_property_block_main_row\"]"));

            for (WebElement webElement : elements) {
                Document document = new Document();

                try {
                    //url
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[1]/div[1]//a")))
                            .map(x -> {
                                return x.getAttribute("href");
                            })
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("url", x);
                            });
                    //判断是否爬取过该列表项
                    String oldUrl = (String) document.get("url");
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "xinjiapo_booking_hotel");
                    if (exists) {
                        log.info("该列表项已经爬取过");
                        continue;
                    }
                } catch (Exception e) {

                }

                try {
                    //名称
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[1]/div[1]//a/span[1]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("title", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //评价详情
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[2]/div[1]/div[1]")))
                            .map(x -> {
                                return x.getAttribute("data-ratings");
                            })
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("score_detail", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //评分
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[2]/div[1]/div[1]/a[1]/div/div[1]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("score", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //评价结果
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[2]/div[1]/div[1]/a[1]/div/div[2]/div[1]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("score_title", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //评分数量
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[2]/div[1]/div[1]/a[1]/div/div[2]/div[2]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("score_number", x);
                            });
                } catch (Exception e) {

                }

                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                document.put("is_craw", false);
                document.put("is_translate",false);
                mongoTemplate.save(document, "xinjiapo_booking_hotel");

            }

            if (page >= maxPage) {
                break;
            }

            next.click();
            Thread.sleep(4000);
            //下一页按钮
            next = webDriver.findElement(By.xpath("//*[@title = \"下一页\"]"));
            page++;

        }

        log.info("已爬完");
    }


    public void crawDetailPage () throws Exception{
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "xinjiapo_booking_hotel");

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
            mongoTemplate.save(document, "xinjiapo_booking_hotel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }

}
