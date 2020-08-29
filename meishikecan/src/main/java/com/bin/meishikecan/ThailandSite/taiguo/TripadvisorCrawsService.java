package com.bin.meishikecan.ThailandSite.taiguo;

import com.bin.meishikecan.utils.MySeleniumUtils;
import com.gargoylesoftware.htmlunit.WebClient;
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
public class TripadvisorCrawsService {

    private String url = "https://th.tripadvisor.com/Search?q=";

    @Autowired
    private MongoTemplate mongoTemplate;

    public void crawListPage() throws Exception {
        List<Document> words = mongoTemplate.findAll(Document.class, "taiguo_city");
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        int wordNum = 1;
        for (int i = 0; i < words.size(); i++) {
            log.info("爬取第" + wordNum + "个地点关键字");
            String name = (String) words.get(i).get("name");
            String pageUrl = url + name;
            webDriver.get(pageUrl);
            Thread.sleep(2000);


            crawType(2, "tripadvisor_hotel", webDriver);
            crawType(3, "tripadvisor_restaurant", webDriver);
            crawType(4, "tripadvisor_travel", webDriver);

            wordNum++;
        }
    }

    /**
     * @param type  爬取类型  2 旅馆  3餐厅  4旅游
     * @param table 表名
     */
    public void crawType(Integer type, String table, WebDriver webDriver) throws Exception {
        switch (type) {
            case 2:
                log.info("开始爬取列表项：旅馆");
                break;
            case 3:
                log.info("开始爬取列表项：餐厅");
                break;
            case 4:
                log.info("开始爬取列表项：旅游");
                break;
            default:
                break;
        }
        WebElement button = webDriver.findElement(By.xpath("//*[@id=\"search-filters\"]/ul/li[" + type + "]"));
        Thread.sleep(1000);
        button.click();
        Thread.sleep(3000);
        Pattern pattern = Pattern.compile("/.*.html");
        //最大页数
        int maxPage = Integer.parseInt(webDriver.findElement(By.xpath("//*[contains(@class,\"pageNumbers\")]/a[last()]")).getText());
        WebElement nextButton = webDriver.findElement(By.xpath("//*[contains(@class,\"ui_button nav next primary\")]"));
        int page = 1;
        while (true) {
            log.info("爬取第" + page + "页");
            //列表项
            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"location-meta-block\"]"));

            for (WebElement webElement : elements) {
                Document document = new Document();

                //url
                Optional.ofNullable(webElement.findElement(By.xpath("./div[1]")))
                        .map(x -> {
                            return x.getAttribute("onclick");
                        })
                        .map(x -> {
                            //正则匹配
                            Matcher matcher = pattern.matcher(x);
                            if (matcher.find()) {
                                return "https://th.tripadvisor.com" + matcher.group();
                            }
                            return "";
                        })
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("url", x);
                        });
                //判断是否爬取过该列表项
                String oldUrl = (String) document.get("url");
                boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, table);
                if (exists) {
//                    log.info("该列表项已经爬取过");
                    continue;
                }

                //名称
                Optional.ofNullable(webElement.findElement(By.xpath("./div[1]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .ifPresent(x -> {
                            document.put("title", x);
                        });

                //地址
                Optional.ofNullable(webElement.findElement(By.xpath("./div[3]/div[1]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .ifPresent(x -> {
                            document.put("address", x);
                        });

                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                document.put("is_craw", false);
                mongoTemplate.save(document, table);
            }

            if (page >= maxPage) {
                break;
            }

            //翻页
            ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView();", nextButton);
            Thread.sleep(1000);
            nextButton.click();
            Thread.sleep(4000);
            //下一页按钮
            nextButton = webDriver.findElement(By.xpath("//*[contains(@class,\"ui_button nav next primary\")]"));
            page++;
        }
    }

    public void crawDetailPageHotel() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("旅店开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "tripadvisor_hotel");
        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(4000);

            //处理数据
            //1.判断是否是泰国数据
//            String title = (String) document.get("title");
//            String address = (String) document.get("address");
//            boolean bool1 = title.contains("ไทย");
//            boolean bool2 = address.contains("ไทย");
//            if (!(bool1 || bool2)) {
//                mongoTemplate.remove(new Query(Criteria.where("_id").is(document.get("_id"))),"tripadvisor_hotel");
//                log.info("删除一条不符合的数据");
//                continue;
//            }


            //保存联系
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"ui_icon phone _2JUCGp0v _37ahZ22y\")]/../span[2]")))
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
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"_3cjYfwwQ\")]")))
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
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"_2GIfqQkr PsUadXSP test-target-tab-Reviews sLZhlLAo Ls68q8e7\")]/span[2]/span/span[1]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("comment_number", x);
                        });
            } catch (Exception e) {
                document.put("comment_number", "0");
            }

            //排名
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"_28eYYeHH\")]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("rank", x);
                        });
            } catch (Exception e) {

            }


            //最低价格
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"CEf5oHnZ\")]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("lower_price", x);
                        });
            } catch (Exception e) {
                try {
                    Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@id,\"taplc_resp_hr_atf_meta_component_0\")]/div/div/div[1]/div[1]/div/div[2]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("lower_price", x);
                            });
                } catch (Exception E) {

                }

            }


            document.put("update_time", LocalDateTime.now());
            document.put("is_craw", true);
            mongoTemplate.save(document, "tripadvisor_hotel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }

    public void crawDetailPageRestan() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("餐厅：开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "tripadvisor_restaurant");
        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(2000);

            //处理数据
            //1.判断是否是泰国数据
//            String title = (String) document.get("title");
//            String address = (String) document.get("address");
//            boolean bool1 = title.contains("ไทย");
//            boolean bool2 = address.contains("ไทย");
//            if (!(bool1 || bool2)) {
//                mongoTemplate.remove(new Query(Criteria.where("_id").is(document.get("_id"))),"tripadvisor_restaurant");
//                log.info("删除一条不符合的数据");
//                continue;
//            }


            //保存联系
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"_2AZp-JQr _2HBN-k68 rZHcZ9a2\")]/../../span[2]")))
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
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"r2Cf69qf\")]")))
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
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"reviews_header_count\")]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("comment_number", x);
                        });
            } catch (Exception e) {
                document.put("comment_number", "0");
            }

            //排名
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"_3-W4EexF\")]")))
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
            mongoTemplate.save(document, "tripadvisor_restaurant");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }


    public void crawDetailPageTravel() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        Pattern pattern = Pattern.compile("[0-9]+");
        log.info("旅游：开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "tripadvisor_travel");
        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(2000);

            //处理数据
            //1.判断是否是泰国数据
//            String title = (String) document.get("title");
//            String address = (String) document.get("address");
//            boolean bool1 = title.contains("ไทย");
//            boolean bool2 = address.contains("ไทย");
//            if (!(bool1 || bool2)) {
//                mongoTemplate.remove(new Query(Criteria.where("_id").is(document.get("_id"))),"tripadvisor_travel");
//                log.info("删除一条不符合的数据");
//                continue;
//            }

            //保存联系
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"ui_icon phone _3D9Qcwoe\")]/../div")))
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
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"_1NKYRldB\")]/span[1]")))
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
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"_2GIfqQkr _2-zZAhSq sLZhlLAo Ls68q8e7\")]/span[2]/span[1]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("comment_number", x);
                        });
            } catch (Exception e) {
                document.put("comment_number", "0");
            }

            //排名
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"eQSJNhO6\")]")))
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
            mongoTemplate.save(document, "tripadvisor_travel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }

}
