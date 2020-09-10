package com.bin.meishikecan.ThailandSite.yuenan;

import com.bin.meishikecan.utils.MySeleniumUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class YuenanIvivuCrawService {

    @Resource
    private MongoTemplate mongoTemplate;

    public void crawListPage() throws Exception{

        WebDriver webDriver = MySeleniumUtils.getWebDriver();

        webDriver.get("https://www.ivivu.com/khach-san-viet-nam?di=2020-09-09&do=2020-09-10");

        Actions actions = new Actions(webDriver);

        Thread.sleep(4000);

        Integer oldNum = 0;

        int num = 0;

        while (true) {

            try {
                webDriver.findElement(By.xpath("//html")).sendKeys(Keys.END);

                WebElement next = webDriver.findElement(By.xpath("//*[@class=\"btn btn-default btn-lg btn-load-more mt-3 ng-binding\"]"));

                ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView()", next);

                actions.moveToElement(next).click().perform();

                Thread.sleep(2000);
            }catch (Exception e) {
                break;
            }

            Integer newNum  = webDriver.findElements(By.xpath("//*[contains(@class,\"hotel-item__wrapper \")]")).size();

            if (newNum.equals(oldNum)) {
                num++;
            }

            if (num > 30) {
                break;
            }

            oldNum = newNum;

        }

        List<WebElement> elements = webDriver.findElements(By.xpath("//*[contains(@class,\"hotel-item__wrapper \")]"));

        for (WebElement webElement : elements) {

            Document document = new Document();

            try {
                //url
                Optional.ofNullable(webElement.findElement(By.xpath("./a")))
                        .map(x-> {
                            return x.getAttribute("href");
                        })
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("url", x);
                        });
                //判断是否爬取过该列表项
                String oldUrl = (String) document.get("url");
                boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "yuenan_ivivu_hotel");
                if (exists) {
                    log.info("该列表项已经爬取过");
                    continue;
                }
            } catch (Exception e) {

            }

            try {
                //名称
                Optional.ofNullable(webElement.findElement(By.xpath("./a//*[@class=\"hotel-item\"]//*[@class=\"center\"]/div[1]/p[1]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .ifPresent(x -> {
                            document.put("title", x);
                        });
            } catch (Exception e) {

            }

            try {
                //評分
                Optional.ofNullable(webElement.findElement(By.xpath(".//*[contains(@class,\"review-score\")]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .ifPresent(x -> {
                            document.put("score", x);
                        });
            } catch (Exception e) {

                document.put("score","0");

            }

            try {
                //地址
                Optional.ofNullable(webElement.findElement(By.xpath(".//*[contains(@class,\"address\")]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .ifPresent(x -> {
                            document.put("address", x);
                        });
            } catch (Exception e) {

            }

            try {
                //價格
                Optional.ofNullable(webElement.findElement(By.xpath(".//*[@class=\"price-num ng-binding\"")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .ifPresent(x -> {
                            document.put("price", x);
                        });
            } catch (Exception e) {

            }

            document.put("create_time",new Date());
            document.put("is_translate",false);
            document.put("is_craw",true);

            mongoTemplate.save(document,"yuenan_ivivu_hotel");

        }

    }

}
