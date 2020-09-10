package com.bin.meishikecan.ThailandSite.yuenan;

import com.bin.meishikecan.utils.MySeleniumUtils;
import com.bin.meishikecan.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
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
public class YuenanTripadvisorCrawsService {

    @Autowired
    private MongoTemplate mongoTemplate;

    private void analyseAndSaveListPage(WebDriver webDriver) {
        List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"geoList\"]/li"));

        for (WebElement element : elements) {
            try {
                Document document = new Document();

                // url
                try {
                    WebElement aEle = element.findElement(By.xpath(".//a"));
                    document.put("url", aEle.getAttribute("href"));

                    // 地区
                    document.put("area", aEle.getText().trim());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(document.getString("url"))), "malaixiya_tripadvisor_restaurant_area");

                if (exists) {
                    log.info("url已经存在,不再重复保存: {}", document.getString("url"));
                    continue;
                }

                mongoTemplate.save(document, "malaixiya_tripadvisor_restaurant_area");
            } catch (Exception e) {
                log.error("解析列表页一个元素失败:", e);
            }
        }
    }

    public void crawListPageRes() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();

        List<Document> areas = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "yuenan_tripadvisor_restaurants_area");

        for (Document area : areas) {
            webDriver.get((String) area.get("url"));

            Thread.sleep(4000);


            WebElement next;

            try {
                next = webDriver.findElement(By.xpath("//*[@class=\"nav next rndBtn ui_button primary taLnk\"]"));
            }catch (Exception e) {
                next = null;
            }


            int page = 1;

            while (next != null || page == 1) {

                log.info(area.get("area") + ":第" + page + "页");

                List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"wQjYiB7z\"]"));

                for (WebElement element : elements) {
                    Document document = new Document();

                    // url
                    try {
                        WebElement aEle = element.findElement(By.xpath(".//a"));
                        document.put("url", aEle.getAttribute("href"));

                        // 名稱
                        String title = aEle.getText().trim();
                        document.put("title", title.replaceAll("[0-9.]+", ""));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(document.getString("url"))), "yuenan_tripadvisor_restaurants");

                    if (exists) {
                        continue;
                    }

                    document.put("create_time", LocalDateTime.now());
                    document.put("update_time", LocalDateTime.now());
                    document.put("is_craw", false);
                    document.put("is_translate", false);

                    mongoTemplate.save(document, "yuenan_tripadvisor_restaurants");

                }


                if (next !=null) {
                    next.click();
                }

                Thread.sleep(5000);

                try {
                    next = webDriver.findElement(By.xpath("//*[@class=\"nav next rndBtn ui_button primary taLnk\"]"));
                } catch (Exception e) {
                    next = null;
                }

                page++;

            }

            area.put("is_craw", true);
            mongoTemplate.save(area, "yuenan_tripadvisor_restaurants_area");
            log.info("当前地区已爬完,下一个地区");
        }

    }

    public void crawListPageTravel() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        webDriver.get("https://www.tripadvisor.com.ph/Search?q=Vietnam&searchSessionId=F3D3C7D9B3943C954EC298B405F840AA1598519425749ssid&geo=293921&sid=5BE3361044553C11E664224531F1A7E51598522880936&blockRedirect=true&ssrc=A&rf=6");
        Pattern pattern = Pattern.compile("[0-9|,]+");
        Thread.sleep(5000);
        WebElement nextButton = webDriver.findElement(By.xpath("//*[text()=\"Next\"]"));

        int page = 1;
        while (nextButton != null) {
            log.info("爬取第" + page + "页");
            //列表项
            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"location-meta-block\"]"));
            Thread.sleep(3000);
            for (WebElement webElement : elements) {
                Document document = new Document();
                try {
                    //名稱
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[1]//span")))
                            .map(WebElement::getText)
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("title", x);
                            });
                    //判断是否爬取过该列表项
                    String oldTitle = (String) document.get("title");
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("title").is(oldTitle)), Document.class, "yuenan_tripadvisor_travel");
                    if (exists) {
                        log.info("该列表项已经爬取过");
                        continue;
                    }
                } catch (Exception e) {

                }

                try {
                    //地址
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[3]/div[1]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("address", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //评分
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[2]/div/span")))
                            .map(x -> {
                                String a = x.getAttribute("class");
                                //正则匹配
                                Matcher matcher = pattern.matcher(a);
                                if (matcher.find()) {
                                    return (Double.parseDouble(matcher.group()) / 10) + "";
                                }
                                return "0";
                            })
                            .map(String::trim)
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("score", x);
                            });
                } catch (Exception e1) {
                    document.put("score", "0");
                }

                //评论数
                try {
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[2]/div/a")))
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


                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                document.put("is_craw", true);
                document.put("is_translate", false);
                mongoTemplate.save(document, "yuenan_tripadvisor_travel");
            }

            for (int i = 0; i < 4; i++) {
                webDriver.findElement(By.xpath("./html")).sendKeys(Keys.END);
            }

            Thread.sleep(2000);

            //翻页
            nextButton.click();


            Thread.sleep(2000);
            //下一页按钮
            try {
                nextButton = webDriver.findElement(By.xpath("//*[text()=\"Next\"]"));
            } catch (Exception e) {
                nextButton = null;
            }
            page++;
        }
    }

    public void crawDetailPageHotel() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        Pattern pattern = Pattern.compile("[0-9|,]+");
        log.info("旅店开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "yuenan_tripadvisor_hotel");
        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(4000);

            //地址

            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"_1sPw_t0w _2oAeY-0w\"]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("address", x);
                        });
            } catch (Exception e) {
                Thread.sleep(3000);
                try {
                    Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"_1sPw_t0w _3sCS_WGO\"]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("address", x);
                            });
                } catch (Exception e1) {

                }

            }


            //保存联系
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"vEwHDg4B _3Z-kyXHr\"]/div")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("contact", x);
                        });
            } catch (Exception e) {

            }

            //评分
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"_3cjYfwwQ\"]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("score", x);
                        });
            } catch (Exception e) {
                try {
                    Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"_15eFvJyR\"]/span[1]")))
                            .map(x -> {
                                String a = x.getAttribute("class");
                                //正则匹配
                                Matcher matcher = pattern.matcher(a);
                                if (matcher.find()) {
                                    return (Double.parseDouble(matcher.group()) / 10) + "";
                                }
                                return "0";
                            })
                            .map(String::trim)
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("score", x);
                            });
                } catch (Exception e1) {
                    document.put("score", "0");
                }
            }


            //评论数
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"_3jEYFo-z\"]")))
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
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"_28eYYeHH\"]")))
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
            mongoTemplate.save(document, "yuenan_tripadvisor_hotel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }

    public void crawDetailPageRestan() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("餐厅：开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "yuenan_tripadvisor_restaurants");
        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(3000);

            //保存地址
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"_213lPF2w _2HBN-k68 rZHcZ9a2\"]/../..")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("address", x);
                        });
            } catch (Exception e) {

            }

            //保存联系
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"_2AZp-JQr _2HBN-k68 rZHcZ9a2\"]/../..")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("contact", x);
                        });
            } catch (Exception e) {

            }

            //评分
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"r2Cf69qf\"]")))
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
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"_10Iv7dOs\"]")))
                        .map(WebElement::getText)
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
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"_13OzAOXO _2VxaSjVD\"]//*[@class=\"_15QfMZ2L\"]/span")))
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
            mongoTemplate.save(document, "yuenan_tripadvisor_restaurants");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }

    public void crawDetailPageTravel() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        Pattern pattern = Pattern.compile("[0-9]+");
        log.info("旅游：开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "xinjiapo_tripadvisor_travel");
        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(3000);

            //保存地址
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class = \"ui_icon map-pin-fill _3D9Qcwoe\"]/..")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("address", x);
                        });
            } catch (Exception e) {

            }

            //保存联系
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class = \"ui_icon phone _3D9Qcwoe\"]/..")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("contact", x);
                        });
            } catch (Exception e) {

            }

            //评分
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class= \"_1NKYRldB\"]/span")))
                        .map(x -> {
                            String a = x.getAttribute("class");
                            //正则匹配
                            Matcher matcher = pattern.matcher(a);
                            if (matcher.find()) {
                                return (Double.parseDouble(matcher.group()) / 10) + "";
                            }
                            return "0";
                        })
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
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class = \"_3WF_jKL7 _1uXQPaAr\"]")))
                        .map(WebElement::getText)
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
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class = \"eQSJNhO6\"]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("rank", x);
                        });
            } catch (Exception e) {

            }

            //价格
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class = \"_3BTxI6f9\"]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("price", x);
                        });
            } catch (Exception e) {

            }


            document.put("update_time", LocalDateTime.now());
            document.put("is_craw", true);
            mongoTemplate.save(document, "xinjiapo_tripadvisor_travel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }

}
