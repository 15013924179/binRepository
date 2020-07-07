package com.bin.meishikecan.ThailandSite;

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
        for (Document word : words) {
            log.info("爬取第" + wordNum + "个地点关键字");
            String name = (String) word.get("name");
            String pageUrl = url + name;
            webDriver.get(pageUrl);
            Thread.sleep(2000);

            crawType(2, "tripadvisor_travel", webDriver);
            crawType(3, "tripadvisor_restaurant", webDriver);
            crawType(4, "tripadvisor_hotel", webDriver);
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
        Thread.sleep(2000);
        Pattern pattern = Pattern.compile("/.*.html");
        //最大页数
        int maxPage = Integer.parseInt(webDriver.findElement(By.xpath("//*[contains(@class,\"pageNumbers\")]/a[last()]")).getText());
        WebElement nextButton = webDriver.findElement(By.xpath("//*[contains(@class,\"ui_button nav next primary\")]"));
        for (int i = 1; i <= maxPage; i++) {
            log.info("爬取第" + i + "页");
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
                    log.info("该列表项已经爬取过");
                    continue;
                }

                //名称
                Optional.ofNullable(webElement)
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

            //翻页
            ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView();", nextButton);
            Thread.sleep(1000);
            nextButton.click();
            Thread.sleep(4000);
            //下一页按钮
            nextButton = webDriver.findElement(By.xpath("//*[contains(@class,\"ui_button nav next primary\")]"));
        }
    }
}
