package com.bin.meishikecan.ThailandSite.xinjiapo;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class CtripCrawsService {

    private String url = "https://you.ctrip.com/sight/singapore53/";

    private String hotelUrl = "https://hotels.ctrip.com/international/Singapore73";

    private String resUrl = "https://you.ctrip.com/restaurantlist/singapore53/";

    @Autowired
    private MongoTemplate mongoTemplate;

    public void crawsListPageByTravel() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();

        Integer maxPage = 80;

        Integer page = 1;

        while (true) {
            log.info("第" + page + "页");

            webDriver.get(url+"s0-p"+page+".html#sightname");

            Thread.sleep(3000);

            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"rdetailbox\"]//dt/a"));

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
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "xinjiapo_ctrip_travel");
                    if (exists) {
                        log.info("该列表项已经爬取过");
                        continue;
                    }
                } catch (Exception e) {

                }

                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                document.put("is_craw", false);
                document.put("is_translate", false);
                mongoTemplate.save(document, "xinjiapo_ctrip_travel");

            }

            if (page >= maxPage) {
                break;
            }

            page++;

        }

        log.info("已爬完");
    }

    public void crawDetailPageByTravel() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "xinjiapo_ctrip_travel");
        Pattern pattern = Pattern.compile("[0-9|,]+");
        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(2000);

            //保存联系
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"baseInfoContent\"]/div/p[text()=\"官方电话\"]/../p[2]")))
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
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"baseInfoContent\"]/div/p[text()=\"官方网站\"]/../p[2]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("site", x);
                        });
            } catch (Exception e) {

            }

            //保存时间
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"baseInfoContent\"]/div/p[text()=\"开放时间\"]/../p[2]")))
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
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"baseInfoContent\"]/div/p[text()=\"景点地址\"]/../p[2]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("address", x);
                        });
            } catch (Exception e) {

            }

            //评价
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"commentScoreNum\"]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("score", x);
                        });
            } catch (Exception e) {
                document.put("score", "0");
            }

            //评价总数
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"commentScoreNum\"]/../../p/span")))
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

            //排行
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"rankText\"]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("rank", x);
                        });
            } catch (Exception e) {

            }
            //保存门票
            try {
                webDriver.switchTo().frame(webDriver.findElement(By.xpath("//*[@id=\"ticket-frame\"]")));
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@id=\"J-Ticket\"]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("ticket", x);
                        });
            } catch (Exception e) {

            }


            document.put("update_time", LocalDateTime.now());
            document.put("is_craw", true);
            mongoTemplate.save(document, "xinjiapo_ctrip_travel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }

    public void crawsListPageByHotel() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();

        webDriver.get(hotelUrl);

        Thread.sleep(3000);

        Integer maxPage = 17;

        Integer page = 1;

        WebElement next = webDriver.findElement(By.xpath("//*[text()=\"下一页\"]"));

        while (true) {
            log.info("第" + page + "页");

            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"J_hlist_item_in hlist_item_in\"]"));

            for (WebElement webElement : elements) {
                Document document = new Document();

                try {
                    //url
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[3]/div[2]//a")))
                            .map(x -> {
                                return x.getAttribute("href");
                            })
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("url", x);
                            });
                    //判断是否爬取过该列表项
                    String oldUrl = (String) document.get("url");
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "xinjiapo_ctrip_hotel");
                    if (exists) {
                        log.info("该列表项已经爬取过");
                        continue;
                    }
                } catch (Exception e) {

                }

                try {
                    //中文名称
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[3]/div[1]//a")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("chinese_title", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //英文名称
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[3]/div[2]//a")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("english_title", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //评分
                    Optional.ofNullable(webElement.findElement(By.xpath("./a/div[1]/span/span")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("score", x);
                            });
                } catch (Exception e) {
                    document.put("score", "0");
                }

                try {
                    //推荐率
                    Optional.ofNullable(webElement.findElement(By.xpath("./a/div[2]/span/span[1]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("recommend_rate", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //评分数量
                    Optional.ofNullable(webElement.findElement(By.xpath("./a/div[2]/span/span[2]")))
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
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[6]/a")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("price", x);
                            });
                } catch (Exception e) {

                }


                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                document.put("is_craw", false);
                document.put("is_translate",false);
                mongoTemplate.save(document, "xinjiapo_ctrip_hotel");

            }

            if (page >= maxPage) {
                break;
            }

            next.click();
            Thread.sleep(8000);
            //下一页按钮
            next = webDriver.findElement(By.xpath("//*[text()=\"下一页\"]"));
            page++;

        }

        log.info("已爬完");
    }

    public void crawDetailPageByHotel() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "xinjiapo_ctrip_hotel");

        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(2000);

            //地址
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"address_text\"]")))
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
            mongoTemplate.save(document, "xinjiapo_ctrip_hotel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }

    public void crawsListPageByRes() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();

        Integer maxPage = 1280;

        Integer page = 1;

        Pattern pattern = Pattern.compile("[0-9|,]+");
        while (true) {
            log.info("第" + page + "页");

            webDriver.get(resUrl+"s0-p"+page+".html");

            Thread.sleep(3000);

            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"rdetailbox\"]"));

            for (WebElement webElement : elements) {
                Document document = new Document();

                try {
                    //url
                    Optional.ofNullable(webElement.findElement(By.xpath("./dl/dt/a")))
                            .map(x -> {
                                return x.getAttribute("href");
                            })
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("url", x);
                            });
                    //判断是否爬取过该列表项
                    String oldUrl = (String) document.get("url");
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "xinjiapo_ctrip_restaurant");
                    if (exists) {
                        log.info("该列表项已经爬取过");
                        continue;
                    }
                } catch (Exception e) {

                }

                try {
                    //名称
                    Optional.ofNullable(webElement.findElement(By.xpath("./dl/dt/a")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("title", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //地址
                    Optional.ofNullable(webElement.findElement(By.xpath("./dl/dd[1]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("address", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //人均
                    Optional.ofNullable(webElement.findElement(By.xpath("./dl/dd[2]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("price", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //评分
                    Optional.ofNullable(webElement.findElement(By.xpath("./ul/li[1]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("score", x);
                            });
                } catch (Exception e) {
                    document.put("score", "0");
                }

                try {
                    //评分数量
                    Optional.ofNullable(webElement.findElement(By.xpath("./ul/li[3]")))
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
                mongoTemplate.save(document, "xinjiapo_ctrip_restaurant");

            }

            if (page >= maxPage) {
                break;
            }

            page++;

        }

        log.info("已爬完");
    }


}
