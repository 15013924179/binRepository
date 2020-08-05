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

/**
 * 该站点是爬取旅行社和导游
 *
 */
@Component
@Slf4j
public class SingaporeTrustCrawsService {

    private String url1 = "https://trust.stb.gov.sg/site/content/tagaem/landing-page/travel-agent.html?service=ALL&type=ALL&status=TA_A&curIndex=";

    private String url2 = "https://trust.stb.gov.sg/site/content/tagaem/landing-page/tourist-guide.html?status=TG_A&curIndex=";

    @Autowired
    private MongoTemplate mongoTemplate;

    public void crawListPage1() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        int maxPage = 79;
        int page = 0;
        webDriver.get(url1+page);
        Thread.sleep(5000);

        while (true) {
            log.info("爬取第" + (page + 1) + "页");
            //列表项
            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"detail\"]"));

            for (WebElement webElement : elements) {
                Document document = new Document();
                try {
                    //title
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[1]")))
                            .map(WebElement::getText)
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("title", x);
                            });
                    //判断是否爬取过该列表项
                    String oldUrl = (String) document.get("title");
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("title").is(oldUrl)), Document.class, "xinjiapo_trust_travel_agency");
                    if (exists) {
                        log.info("该列表项已经爬取过");
                        continue;
                    }
                } catch (Exception e) {

                }

                try {
                    //地址
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[2]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("address", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //编号
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[3]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("number", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //许可证类型
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[4]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("license_type", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //电话
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[5]/ul/li[1]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("phone", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //邮箱
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[5]/ul/li[2]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("email", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //网站
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[5]/ul/li[3]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("website", x);
                            });
                } catch (Exception e) {

                }

                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                document.put("is_translate", false);
                mongoTemplate.save(document, "xinjiapo_trust_travel_agency");
            }

            if (page >= maxPage) {
                break;
            }
            page++;
            webDriver.get(url1+page);
            Thread.sleep(4000);
            //下一页
        }
    }

    public void crawListPage2() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        int maxPage = 199;
        int page = 0;
        webDriver.get(url2+page);
        Thread.sleep(5000);

        while (true) {
            log.info("爬取第" + (page + 1) + "页");
            //列表项
            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"grid-view\"]"));

            for (WebElement webElement : elements) {
                Document document = new Document();
                try {
                    //名字
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[2]")))
                            .map(WebElement::getText)
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("name", x);
                            });
                    //判断是否爬取过该列表项
                    String oldUrl = (String) document.get("name");
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("name").is(oldUrl)), Document.class, "xinjiapo_trust_guide");
                    if (exists) {
                        log.info("该列表项已经爬取过");
                        continue;
                    }
                } catch (Exception e) {

                }

                try {
                    //分类
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[3]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("category", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //语言
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[4]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("language", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //电话
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[5]/a[1]")))
                            .map(x -> {
                                return x.getAttribute("data-original-title");
                            })
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("phone", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //邮箱
                    Optional.ofNullable(webElement.findElement(By.xpath("./div[5]/a[2]")))
                            .map(x -> {
                                return x.getAttribute("data-original-title");
                            })
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("email", x);
                            });
                } catch (Exception e) {

                }
                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                document.put("is_translate", false);
                mongoTemplate.save(document, "xinjiapo_trust_guide");
            }

            if (page >= maxPage) {
                break;
            }
            page++;
            webDriver.get(url2+page);
            Thread.sleep(2000);
            //下一页
        }
    }

}
