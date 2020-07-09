package com.bin.meishikecan.ThailandSite;

import com.bin.meishikecan.utils.MySeleniumUtils;
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

@Component
@Slf4j
public class SkyscannerCrawService {
    private String url = "https://www.skyscanner.co.th/hotels/search?entity_id=27536671";

    @Autowired
    private MongoTemplate mongoTemplate;

    public void crawListPage() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        webDriver.get(url);
        Thread.sleep(30000);
        List<Document> citys = mongoTemplate.findAll(Document.class, "taiguo_city");
        //关键字
        int index = 1;
        int oldElement = 0;
        //给五次缓存机会
        int cach = 5;
        for (Document city : citys) {
            log.info("开始爬取第" + index + "关键字");
            WebElement input = webDriver.findElement(By.xpath("//*[@id=\"destination-autosuggest\"]"));
            input.sendKeys((String) city.get("name"));
            WebElement button = webDriver.findElement(By.xpath("//*[@class=\"BpkButton_bpk-button__3CLCx BpkButton_bpk-button--large__3nGhA SearchControls_SearchControls__cta__3nH4P\"]"));
            button.click();
            Thread.sleep(5000);

            //翻页
            log.info("开始翻页");


            while (true) {
                try {
                    WebElement next = webDriver.findElement(By.xpath("//*[@class=\"HotelCardsList_HotelCardsList__bottomButton__KIY-v\"]/button[text()=\"ดูเพิ่มเติม\"]"));
                    next.click();
                    Thread.sleep(5000);
                    log.info("存在更多按钮，点击");
                } catch (Exception e) {

                }
                webDriver.findElement(By.tagName("html")).sendKeys(Keys.END);
                Thread.sleep(5000);
                int newElement = webDriver.findElements(By.xpath("//*[@class=\"CardRowLayout_CardRowLayout__3d3Kh\"]")).size();
                log.info("列表数量：" + newElement);
                if (newElement == oldElement) {
                    --cach;
                    if (cach < 0) {
                        break;
                    }
                } else {
                    oldElement = newElement;
                }
            }

            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"CardRowLayout_CardRowLayout__3d3Kh\"]"));

            log.info("开始保存列表页数据");
            for (WebElement webElement : elements) {
                Document document = new Document();


                try {
                    //url
                    Optional.ofNullable(webElement.findElement(By.xpath("./div/div[1]/a")))
                            .map(x -> {
                                return x.getAttribute("href");
                            })
                            .map(x -> {return x.split("\\?")[0];})
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("url", x);
                            });
                    //判断是否爬取过该列表项
                    String oldUrl = (String) document.get("url");
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "skyscanner_hotel");
                    if (exists) {
                        log.info("该列表项已经爬取过");
                        continue;
                    }

                } catch (Exception e) {

                }

                try {
                    //标题
                    Optional.ofNullable(webElement.findElement(By.xpath("./div/div[1]/a/div[1]/div[1]/span")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("title", x);
                            });
                } catch (Exception e) {

                }

                try {
                    //评分数
                    Optional.ofNullable(webElement.findElement(By.xpath("./div/div[1]/a/div[1]/div[3]/span")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("score", x);
                            });

                } catch (Exception e) {

                }

                try {
                    //最低价格
                    Optional.ofNullable(webElement.findElement(By.xpath("./div/div[3]/a/div[1]/div[3]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .ifPresent(x -> {
                                document.put("lower_price", x);
                            });
                } catch (Exception e) {

                }

                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                document.put("is_craw", false);
                mongoTemplate.save(document, "skyscanner_hotel");
            }

        }
    }

}
