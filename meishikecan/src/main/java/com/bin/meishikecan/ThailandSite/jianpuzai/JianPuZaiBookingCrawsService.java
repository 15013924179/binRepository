package com.bin.meishikecan.ThailandSite.jianpuzai;

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
public class JianPuZaiBookingCrawsService {

    @Autowired
    private MongoTemplate mongoTemplate;

    private String url = "https://www.booking.com/searchresults.en-gb.html?aid=1815331&label=kh-autFviVTnRQTouyT6JCpBwS381452403077%3Apl%3Ata%3Ap1%3Ap2%3Aac%3Aap%3Aneg%3Afi%3Atiaud-294889296213%3Akwd-16182756352%3Alp9066820%3Ali%3Adec%3Adm%3Appccp%3DUmFuZG9tSVYkc2RlIyh9YYzu_e_JczYp9KPuwt_Sn0E&lang=en-gb&sid=3e507b59d1aeefb4e72d9dc447e56916&sb=1&sb_lp=1&src=country&src_elem=sb&error_url=https%3A%2F%2Fwww.booking.com%2Fcountry%2Fkh.en-gb.html%3Faid%3D1815331%3Blabel%3Dkh-autFviVTnRQTouyT6JCpBwS381452403077%253Apl%253Ata%253Ap1%253Ap2%253Aac%253Aap%253Aneg%253Afi%253Atiaud-294889296213%253Akwd-16182756352%253Alp9066820%253Ali%253Adec%253Adm%253Appccp%253DUmFuZG9tSVYkc2RlIyh9YYzu_e_JczYp9KPuwt_Sn0E%3Bsid%3D3e507b59d1aeefb4e72d9dc447e56916%3B&ss=Cambodia&is_ski_area=0&checkin_year=2020&checkin_month=9&checkin_monthday=1&checkout_year=2020&checkout_month=9&checkout_monthday=2&group_adults=2&group_children=0&no_rooms=1&b_h4u_keep_filters=&from_sf=1&ss_raw=Cambodia&ac_position=0&ac_langcode=en&ac_click_type=b&dest_id=36&dest_type=country&place_id_lat=12.5657&place_id_lon=104.991&search_pageview_id=d1a01869fd7300fd&search_selected=true";

    public void crawsListPage() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();

        webDriver.get(url);

        Thread.sleep(3000);

        Integer maxPage = 40;

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
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "jianpuzai_booking_hotel");
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
                mongoTemplate.save(document, "jianpuzai_booking_hotel");

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
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "jianpuzai_booking_hotel");

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
            mongoTemplate.save(document, "jianpuzai_booking_hotel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }

}
