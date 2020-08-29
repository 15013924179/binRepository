package com.bin.meishikecan.ThailandSite.malaixiya;


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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class MalaixiyaMaFengWoCrawsService {


    @Autowired
    private MongoTemplate mongoTemplate;

    public void crawsListPageHotel() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriverHavingImg();

        Pattern pattern = Pattern.compile("[0-9|,]+");

        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "malaixiya_mafengwo_area");

        for (Document area : documents) {
            String title = (String) area.get("title");
            String url = (String) area.get("url");

            log.info("开始爬取：" + title);

            webDriver.get(url);

            while (true) {
                try {
                    webDriver.findElement(By.xpath("//*[@class=\"dialog-container dialog-container-show\"]"));
                    Thread.sleep(5000);
                    log.info("反爬中,循环等待操作...");
                }catch (Exception e) {
                    break;
                }
            }

            Thread.sleep(8000);

            WebElement next;

            try {
                next = webDriver.findElement(By.xpath("//*[text()=\"后一页\"]"));
            }catch (Exception e) {
                next = null;
            }


            int page = 1;

            while (next != null || page == 1) {
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
                        boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "malaixiya_mafengwo_hotel");
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
                        Optional.ofNullable(webElement.findElement(By.xpath(".//*[contains(@class,\"rating\")]/em")))
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
                        Optional.ofNullable(webElement.findElement(By.xpath(".//*[text()=\"蜂蜂评价\"]/em")))
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
                        //游记提及
                        Optional.ofNullable(webElement.findElement(By.xpath(".//*[text()=\"游记提及\"]/em")))
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
                                    document.put("travel_note_number", x);
                                });
                    } catch (Exception e) {
                        document.put("travel_note_number", "0");
                    }

                    try {
                        //价格
                        WebElement priceElement = webElement.findElement(By.xpath("./div[4]"));
                        document.put("price", priceElement.getText().trim());
                    } catch (Exception e) {

                    }


                    document.put("create_time", LocalDateTime.now());
                    document.put("update_time", LocalDateTime.now());
                    document.put("is_craw", false);
                    document.put("is_translate", false);
                    mongoTemplate.save(document, "malaixiya_mafengwo_hotel");

                }

                if (next !=null) {
                    next.click();
                }

                Thread.sleep(4000);
                for (int i = 0; i < 4; i++) {
                    webDriver.findElement(By.xpath("//html")).sendKeys(Keys.END);
                    Thread.sleep(1000);
                }

                //下一页按钮
                try {
                    next = webDriver.findElement(By.xpath("//*[text() = \"后一页\"]"));
                } catch (Exception e) {
                    next = null;
                }
                page++;

            }

            area.put("is_craw", true);

            mongoTemplate.save(area, "malaixiya_mafengwo_area");

        }

    }

    public void crawDetailPageHotel() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "malaixiya_mafengwo_hotel");

        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(2000);

            //地址
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("/html/body/div[2]/div[2]/div[1]/div[4]/span")))
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
            mongoTemplate.save(document, "malaixiya_mafengwo_hotel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }

    public void crawListPageTravel() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();

        List<Document> areas = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "malaixiya_mafengwo_travel_area");

        for (Document area : areas) {

            String title = (String) area.get("title");

            log.info("爬取地点：" + title);

            webDriver.get((String) area.get("url"));

            Thread.sleep(5000);

            WebElement next;

            try {
                next = webDriver.findElement(By.xpath("//*[text()=\"后一页\"]"));
            } catch (Exception e) {
                next = null;
            }

            int page = 1;

            while (next != null || page == 1) {

                log.info("第"+page+"页");

                List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"scenic-list clearfix\"]/li/a"));

                for (WebElement webElement : elements) {
                    Document document = new Document();

                    try {
                        String url = webElement.getAttribute("href");
                        document.put("url",url);
                        //判断是否爬取过该列表项
                        String oldUrl = (String) document.get("url");
                        boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "malaixiya_mafengwo_travel");
                        if (exists) {
                            log.info("该列表项已经爬取过");
                            continue;
                        }
                    }catch (Exception e) {

                    }


                    try {
                        String name = webElement.getAttribute("title").trim();
                        document.put("title",name);
                    }catch (Exception e) {

                    }

                    document.put("create_time", LocalDateTime.now());
                    document.put("update_time", LocalDateTime.now());
                    document.put("is_craw", false);
                    document.put("is_translate", false);
                    mongoTemplate.save(document, "malaixiya_mafengwo_travel");

                }

                if (next !=null) {
                    next.click();
                }

                Thread.sleep(5000);

                try {
                    next = webDriver.findElement(By.xpath("//*[text()=\"后一页\"]"));
                } catch (Exception e) {
                    next = null;
                }

                page++;

            }

            area.put("is_craw",true);

            mongoTemplate.save(area, "malaixiya_mafengwo_travel_area");

        }

    }

    public void crawDetailPageTravel() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("开始爬取详情页");

        Pattern pattern = Pattern.compile("[0-9|,]+");

        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "malaixiya_mafengwo_travel");

        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(5000);

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
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"review-nav\"]//*[text()=\"好评\"]/../span[2]")))
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
                            document.put("good_score_number", x);
                        });
            } catch (Exception e) {
                document.put("good_score_number", "0");
            }

            //中评总数
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"review-nav\"]//*[text()=\"中评\"]/../span[2]")))
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
                            document.put("medium_score_number", x);
                        });
            } catch (Exception e) {
                document.put("medium_score_number", "0");
            }

            //差评总数
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"review-nav\"]//*[text()=\"差评\"]/../span[2]")))
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
                            document.put("bad_score_number", x);
                        });
            } catch (Exception e) {
                document.put("bad_score_number", "0");
            }


            document.put("update_time", LocalDateTime.now());
            document.put("is_craw", true);
            mongoTemplate.save(document, "malaixiya_mafengwo_travel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }


}
