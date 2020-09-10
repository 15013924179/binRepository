package com.bin.meishikecan.ThailandSite;

import com.bin.meishikecan.utils.MySeleniumUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
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
public class CrawTemplateService {

    @Resource
    private MongoTemplate mongoTemplate;

    public void bookingListPage(String url, String table) throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();

        webDriver.get(url);

        Thread.sleep(3000);

        Integer page = 1;

        WebElement next = webDriver.findElement(By.xpath("//*[@class= \"bui-pagination__item bui-pagination__next-arrow\"]/a"));

        while (next != null) {
            log.info("第" + page + "页");

            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class= \"sr_item_content sr_item_content_slider_wrapper \"]"));

            for (WebElement webElement : elements) {
                Document document = new Document();

                try {
                    //url
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[1]/div[1]/div[1]//a")))
                            .map(x -> {
                                String s = x.getAttribute("href");
                                return s.split("\\?")[0];
                            })
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("url", x);
                            });
                    //判断是否爬取过该列表项
                    String oldUrl = (String) document.get("url");
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, table);
                    if (exists) {
                        log.info("该列表项已经爬取过");
                        continue;
                    }
                } catch (Exception e) {

                }

                try {
                    //名称
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[1]/div[1]/div[1]//a/span[1]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("title", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //评分
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[1]/div[2]/div[1]/div[1]/a[1]/div/div[1]")))
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
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[1]/div[2]/div[1]/div[1]/a[1]/div/div[2]/div[2]")))
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
                    Optional.ofNullable(webElement.findElement(By.xpath(".//*[@class=\"bui-price-display__value prco-inline-block-maker-helper\"]")))
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
                document.put("is_translate", false);
                mongoTemplate.save(document, table);

            }

            next.click();

            Thread.sleep(4000);
            //下一页按钮
            try {
                next = webDriver.findElement(By.xpath("//*[@class= \"bui-pagination__item bui-pagination__next-arrow\"]/a"));
            } catch (Exception e) {
                next = null;
            }

            page++;

        }

        log.info("已爬完");
    }

    public void bookingDetailPage(String table) throws Exception {

        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, table);

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
            mongoTemplate.save(document, table);
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");

    }

    public void tripadvisorListPageByHotel(String url, String table) throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        webDriver.get(url);
        Thread.sleep(5000);
        Actions ac = new Actions(webDriver);

        WebElement nextButton = webDriver.findElement(By.xpath("//*[contains(@class,\"unified ui_pagination standard_pagination ui_section listFooter\")]//*[@class=\"nav next ui_button primary  cx_brand_refresh_phase2\"]"));
        int page = 1;
        while (nextButton != null) {
            log.info("爬取第" + page + "页");
            //列表项
            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"ui_column is-8 main_col allowEllipsis\"]"));

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
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, table);
                    if (exists) {
                        log.info("该列表项已经爬取过");
                        continue;
                    }
                } catch (Exception e) {

                }

                try {
                    //名称
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[1]/div/a")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("title", x);
                            });
                } catch (Exception e) {

                }


                try {
                    //价格
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[2]/div[1]/div/div/div[1]/div/div[1]")))
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
                document.put("is_translate", false);
                mongoTemplate.save(document, table);
            }

            for (int i = 0; i < 4; i++) {
                webDriver.findElement(By.xpath("./html")).sendKeys(Keys.END);
            }

            for (int i = 0; i < 10; i++) {
                try{
                    ac.moveToElement(nextButton).click().perform();

                    break;
                }catch (Exception e) {

                    log.info("点击重试");

                }
            }

            Thread.sleep(5000);
            //下一页按钮
            try {
                nextButton = webDriver.findElement(By.xpath("//*[contains(@class,\"unified ui_pagination standard_pagination ui_section listFooter\")]//*[@class=\"nav next ui_button primary  cx_brand_refresh_phase2\"]"));
            } catch (Exception e) {
                nextButton = null;
            }

            page++;
        }
    }

    public void tripadvisorDetailPageByHotel(String table) throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        Pattern pattern = Pattern.compile("[0-9|,]+");
        log.info("旅店开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, table);
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
            mongoTemplate.save(document, table);
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }

    public void tripadvisorListPageByRes(String url,String table)throws Exception {

//        tripadvisorcrawListPageResArea(url,table);

        WebDriver webDriver = MySeleniumUtils.getWebDriver();

        List<Document> areas = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, table+"_area");

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
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(document.getString("url"))), table);

                    if (exists) {
                        continue;
                    }

                    document.put("create_time", LocalDateTime.now());
                    document.put("update_time", LocalDateTime.now());
                    document.put("is_craw", false);
                    document.put("is_translate", false);

                    mongoTemplate.save(document, table);

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
            mongoTemplate.save(area, table+"_area");
            log.info("当前地区已爬完,下一个地区");
        }

    }

    private void tripadvisorcrawListPageResArea(String url,String table) throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();

        webDriver.get(url);

        Thread.sleep(5000);

        //第一页的处理
        List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"geo_name\"]"));

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
                boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(document.getString("url"))), table+"_area");

                if (exists) {
                    log.info("url已经存在,不再重复保存: {}", document.getString("url"));
                    continue;
                }

                document.put("is_craw",false);

                mongoTemplate.save(document, table+"_area");
            } catch (Exception e) {
                log.error("解析列表页一个元素失败:", e);
            }
        }

        WebElement webElement = webDriver.findElement(By.xpath("//*[@id=\"SDTOPDESTCONTENT\"]/div[2]/div/a"));

        while (webElement != null) {

            webElement.click();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //第2頁的處理
            tripadvisoranalyseAndSaveListPage(webDriver,table);

            try {
                webElement = webDriver.findElement(By.xpath("//*[@class=\"guiArw sprite-pageNext  pid0\"]"));
            } catch (Exception e) {
                webElement = null;
            }
        }

        log.info("解析地区完成,关闭webDriver,开始解析餐厅");
        webDriver.close();

    }

    private void tripadvisoranalyseAndSaveListPage(WebDriver webDriver,String table) {
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
                boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(document.getString("url"))), table+"_area");

                if (exists) {
                    log.info("url已经存在,不再重复保存: {}", document.getString("url"));
                    continue;
                }

                mongoTemplate.save(document, table+"_area");
            } catch (Exception e) {
                log.error("解析列表页一个元素失败:", e);
            }
        }
    }

    public void tripadvisorDetailPageRes(String table) throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("餐厅：开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, table);
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
            mongoTemplate.save(document, table);
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }

    public void tripadvisorListPageTravel(String url,String table) throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        webDriver.get(url);
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
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("title").is(oldTitle)), Document.class, table);
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
                mongoTemplate.save(document, table);
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


}
