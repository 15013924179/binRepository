package com.bin.meishikecan.ThailandSite.xinjiapo;


import com.bin.meishikecan.utils.MySeleniumUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
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
public class MaFengWoCrawsService {

    private String url = "http://www.mafengwo.cn/jd/10754/gonglve.html";

    private String hotelUrl = "http://www.mafengwo.cn/hotel/10754/?sFrom=mdd";

    @Autowired
    private MongoTemplate mongoTemplate;

    public void crawsListPage() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();

        webDriver.get(url);

        Thread.sleep(3000);

        Integer maxPage = 20;

        Integer page = 1;

        WebElement next = webDriver.findElement(By.xpath("//*[@title = \"后一页\"]"));

        while (true) {
            log.info("第" + page + "页");

            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"scenic-list clearfix\"]/li/a"));

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
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "xinjiapo_mafengwo_travel");
                    if (exists) {
                        log.info("该列表项已经爬取过");
                        continue;
                    }
                } catch (Exception e) {

                }

                try {
                    //名称
                    Optional.ofNullable(webElement)
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("title", x);
                            });
                } catch (Exception e) {

                }

                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                document.put("is_craw", false);
                document.put("is_translate", false);
                mongoTemplate.save(document, "xinjiapo_mafengwo_travel");

            }

            if (page >= maxPage) {
                break;
            }

            next.click();
            Thread.sleep(4000);
            //下一页按钮
            next = webDriver.findElement(By.xpath("//*[@title = \"后一页\"]"));
            page++;

        }

        log.info("已爬完");
    }

    public void crawDetailPage() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "xinjiapo_mafengwo_travel");

        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(2000);

            //保存联系
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"tel\"]/div[2]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("contact", x);
                        });
            } catch (Exception e) {

            }

            //保存网站
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"item-site\"]/div[2]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("site", x);
                        });
            } catch (Exception e) {

            }

            //保存交通
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"mod mod-detail\"]//dt[text()=\"交通\"]/../dd")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("traffic", x);
                        });
            } catch (Exception e) {

            }

            //保存门票
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"mod mod-detail\"]//dt[text()=\"门票\"]/../dd")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("ticket", x);
                        });
            } catch (Exception e) {

            }

            //保存时间
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"mod mod-detail\"]//dt[text()=\"开放时间\"]/../dd")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("open_time", x);
                        });
            } catch (Exception e) {

            }

            //地址
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"mod mod-location\"]/div[1]/p")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("address", x);
                        });
            } catch (Exception e) {

            }

            //评价总数
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"mhd mhd-large\"]//em")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("all_score_number", x);
                        });
            } catch (Exception e) {
                document.put("all_score_number", "0");
            }

            //好评总数
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"review-nav\"]/ul/li[3]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("good_score_number", x);
                        });
            } catch (Exception e) {
                document.put("good_score_number", "0");
            }

            //中评总数
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"review-nav\"]/ul/li[4]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("medium_score_number", x);
                        });
            } catch (Exception e) {
                document.put("medium_score_number", "0");
            }

            //差评总数
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"review-nav\"]/ul/li[5]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("bad_score_number", x);
                        });
            } catch (Exception e) {
                document.put("bad_score_number", "0");
            }


            document.put("update_time", LocalDateTime.now());
            document.put("is_craw", true);
            mongoTemplate.save(document, "xinjiapo_mafengwo_travel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }

    public void crawListPageByHotel() throws Exception {

        WebDriver webDriver = MySeleniumUtils.getWebDriver();

        webDriver.get(hotelUrl);

        Thread.sleep(3000);

        Integer maxPage = 29;

        Integer page = 1;

        WebElement next = webDriver.findElement(By.xpath("//*[text() = \"后一页\"]"));

        while (true) {
            log.info("第" + page + "页");

            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"hotel-item clearfix _j_hotel_item\"]"));

            for (WebElement webElement : elements) {
                Document document = new Document();

                try {
                    //url
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[2]/div/h3/a")))
                            .map(x -> {
                                return x.getAttribute("href");
                            })
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("url", x);
                            });
                    //判断是否爬取过该列表项
                    String oldUrl = (String) document.get("url");
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "xinjiapo_mafengwo_hotel");
                    if (exists) {
                        log.info("该列表项已经爬取过");
                        continue;
                    }
                } catch (Exception e) {

                }

                try {
                    //中文名称
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[2]/div/h3/a")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("chinese_title", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //英文名称
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[2]/div/span")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("english_title", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //評分
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[3]/ul/li[1]/em")))
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
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[3]/ul/li[2]//em")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("score_number", x);
                            });
                } catch (Exception e) {
                    document.put("score_number", "0");
                }

                try {
                    //价格
                    List<WebElement> priceElement = webElement.findElements(By.xpath("./div[4]/a/div[2]/strong[2]"));
                    String price = "";
                    for (WebElement e : priceElement) {
                        price =price + "￥" + e.getText() + "|";
                    }
                    document.put("price", price);
                } catch (Exception e) {

                }





                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                document.put("is_craw", false);
                document.put("is_translate", false);
                mongoTemplate.save(document, "xinjiapo_mafengwo_hotel");

            }

            if (page >= maxPage) {
                break;
            }

            next.click();
            Thread.sleep(4000);
            for (int i = 0;i<4;i++) {
                webDriver.findElement(By.xpath("//html")).sendKeys(Keys.END);
                Thread.sleep(1000);
            }

            //下一页按钮
            next = webDriver.findElement(By.xpath("//*[text() = \"后一页\"]"));
            page++;

        }

        log.info("已爬完");

    }

    public void crawDetailPageByHotel() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "xinjiapo_mafengwo_hotel");

        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(2000);

            //地址
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class = \"location\"]")))
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
            mongoTemplate.save(document, "xinjiapo_mafengwo_hotel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }


}
