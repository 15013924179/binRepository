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
public class SingaporeTripadvisorCrawsService {

    private String url = "https://www.tripadvisor.com.sg/Hotels-g294262-Singapore-Hotels.html";

    private String Resurl = "https://www.tripadvisor.com.sg/Restaurants-g294265-Singapore.html";

    private String TravelUrl = "https://www.tripadvisor.com.sg/Attractions-g294265-Activities-oa30-Singapore.html";

    @Autowired
    private MongoTemplate mongoTemplate;

    public void crawListPageHotel() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        webDriver.get(url);
        Thread.sleep(5000);
        Pattern pattern = Pattern.compile("/.*.html");
        //最大页数
        int maxPage = Integer.parseInt(webDriver.findElement(By.xpath("//*[contains(@class,\"unified ui_pagination standard_pagination ui_section listFooter\")]/div/span[last()]")).getText());
        WebElement nextButton = webDriver.findElement(By.xpath("//*[contains(@class,\"unified ui_pagination standard_pagination ui_section listFooter\")]/span[2]"));
        int page = 1;
        while (true) {
            log.info("爬取第" + page + "页");
            //列表项
            List<WebElement> elements = webDriver.findElements(By.xpath("//*[contains(@class,\"meta_listing ui_columns large_thumbnail_mobile \")]/div[2]"));
            Thread.sleep(3000);
            for (WebElement webElement : elements) {
                Document document = new Document();
                try {
                    //url
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[1]/div/a")))
                            .map(x -> {
                                return x.getAttribute("href");
                            })
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("url", x);
                            });
                    //判断是否爬取过该列表项
                    String oldUrl = (String) document.get("url");
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "xinjiapo_tripadvisor_hotel");
                    if (exists) {
                        log.info("该列表项已经爬取过");
                        continue;
                    }
                }catch (Exception e){

                }

                try {
                    //名称
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[1]/div/a")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("title", x);
                            });
                }catch (Exception e) {

                }


                try{
                    //价格
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[2]/div[1]/div/div/div[1]/div/div[1]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("price", x);
                            });
                }catch (Exception e) {

                }


                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                document.put("is_craw", false);
                document.put("is_translate",false);
                mongoTemplate.save(document, "xinjiapo_tripadvisor_hotel");
            }

            if (page >= maxPage) {
                break;
            }

            //翻页
//            ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView();", nextButton);
//            Thread.sleep(2000);
            nextButton.click();
            Thread.sleep(4000);
            //下一页按钮
            nextButton = webDriver.findElement(By.xpath("//*[contains(@class,\"unified ui_pagination standard_pagination ui_section listFooter\")]/span[2]"));
            page++;
        }
    }

    public void crawListPageRes() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        webDriver.get(Resurl);
        Thread.sleep(5000);
        //最大页数
        int maxPage = 396;
        WebElement nextButton = webDriver.findElement(By.xpath("//*[@class=\"nav next rndBtn ui_button primary taLnk\"]"));

        int page = 1;
        while (true) {
            log.info("爬取第" + page + "页");
            //列表项
            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"wQjYiB7z\"]"));
            Thread.sleep(3000);
            for (WebElement webElement : elements) {
                Document document = new Document();
                try {
                    //url
                    Optional.ofNullable(webElement.findElement(By.xpath("./span/a")))
                            .map(x -> {
                                return x.getAttribute("href");
                            })
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("url", x);
                            });
                    //判断是否爬取过该列表项
                    String oldUrl = (String) document.get("url");
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "xinjiapo_tripadvisor_restaurant");
                    if (exists) {
                        log.info("该列表项已经爬取过");
                        continue;
                    }
                }catch (Exception e){

                }

                try {
                    //名称
                    Optional.ofNullable(webElement.findElement(By.xpath("./span/a")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("title", x);
                            });
                }catch (Exception e) {

                }


                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                document.put("is_craw", false);
                document.put("is_translate",false);
                mongoTemplate.save(document, "xinjiapo_tripadvisor_restaurant");
            }

            if (page >= maxPage) {
                break;
            }

            webDriver.findElement(By.xpath("./html")).sendKeys(Keys.END);
            //翻页
            while(true) {
                try {
                    Thread.sleep(2000);
                    nextButton.click();
                    break;
                }catch (Exception e) {
                    log.info("翻页异常,继续尝试");
                    nextButton = webDriver.findElement(By.xpath("//*[@class=\"nav next rndBtn ui_button primary taLnk\"]"));
                }
            }

            Thread.sleep(4000);
            //下一页按钮
            nextButton = webDriver.findElement(By.xpath("//*[@class=\"nav next rndBtn ui_button primary taLnk\"]"));
            page++;
        }
    }

    public void crawListPageTravel() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        webDriver.get(TravelUrl);
        Thread.sleep(5000);
        //最大页数
        int maxPage = 34;
        WebElement nextButton = webDriver.findElement(By.xpath("//*[@class=\"ui_button nav next primary \"]"));

        int page = 1;
        while (true) {
            log.info("爬取第" + page + "页");
            //列表项
            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"_1QKQOve4\"]"));
            Thread.sleep(3000);
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
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "xinjiapo_tripadvisor_travel");
                    if (exists) {
                        log.info("该列表项已经爬取过");
                        continue;
                    }
                }catch (Exception e){

                }

                try {
                    //名称
                    Optional.ofNullable(webElement)
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("title", x);
                            });
                }catch (Exception e) {

                }


                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                document.put("is_craw", false);
                document.put("is_translate",false);
                mongoTemplate.save(document, "xinjiapo_tripadvisor_travel");
            }

            if (page >= maxPage) {
                break;
            }

            for (int i=0;i<4;i++) {
                webDriver.findElement(By.xpath("./html")).sendKeys(Keys.END);
            }

            Thread.sleep(2000);

            //翻页
            nextButton.click();
            Thread.sleep(2000);
            //下一页按钮
            nextButton = webDriver.findElement(By.xpath("//*[@class=\"ui_button nav next primary \"]"));
            page++;
        }
    }

    public void crawDetailPageHotel() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("旅店开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "xinjiapo_tripadvisor_hotel");
        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(4000);

            //地址
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"vEwHDg4B _1WEIRhGY\"]/div/span[2]")))
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
                document.put("score", "0");
            }

            //评论数
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"_3jEYFo-z\"]")))
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
            mongoTemplate.save(document, "xinjiapo_tripadvisor_hotel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }

    public void crawDetailPageRestan() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("餐厅：开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "xinjiapo_tripadvisor_restaurant");
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
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@id=\"component_43\"]/div/div[2]/span[2]")))
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
            mongoTemplate.save(document, "xinjiapo_tripadvisor_restaurant");
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
                            String a=x.getAttribute("class");
                            //正则匹配
                            Matcher matcher = pattern.matcher(a);
                            if (matcher.find()) {
                                return (Double.parseDouble(matcher.group())/10)+"";
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
