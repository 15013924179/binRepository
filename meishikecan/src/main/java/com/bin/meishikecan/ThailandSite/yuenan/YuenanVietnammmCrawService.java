package com.bin.meishikecan.ThailandSite.yuenan;


import com.bin.meishikecan.utils.MySeleniumUtils;
import com.bin.meishikecan.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class YuenanVietnammmCrawService {

    @Resource
    private MongoTemplate mongoTemplate;


    public void crawListPage() throws Exception{

        WebDriver webDriver = MySeleniumUtils.getWebDriver();

        List<Document> areas = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "yuenan_vietnammm_area");

        for (Document area : areas) {
            webDriver.get((String) area.get("url"));

            Thread.sleep(4000);

            for (int i = 0; i < 4; i++) {
                webDriver.findElement(By.xpath("//html")).sendKeys(Keys.END);
                Thread.sleep(1000);
            }

            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"detailswrapper grid-13\"]"));

            for (WebElement element : elements) {
                Document document = new Document();

                // url
                try {
                    WebElement aEle = element.findElement(By.xpath(".//a"));
                    document.put("url", aEle.getAttribute("href"));

                    // 名稱
                    String title = aEle.getText().trim();
                    document.put("title", title);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(document.getString("url"))), "yuenan_vietnammm_restaurants");

                if (exists) {
                    continue;
                }

                //评分数
                try{
                    String score_number = element.findElement(By.xpath(".//*[@class=\"rating-total\"]")).getText().trim();
                    document.put("score_number",score_number.replaceAll("[()]", ""));

                }catch (Exception e) {

                }

                //评分
                try{
                    String score = element.findElement(By.xpath(".//*[@class=\"review-stars-range\"]")).getAttribute("style").trim();
                    score = score.replaceAll("[^0-9]", "");
                    if (Integer.parseInt(score)>100) {
                        score = "100";
                    }
                    document.put("score",score+"%");

                }catch (Exception e) {

                }

                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                document.put("is_craw", false);
                document.put("is_translate", false);

                mongoTemplate.save(document, "yuenan_vietnammm_restaurants");

            }

            Thread.sleep(5000);

            area.put("is_craw", true);
            mongoTemplate.save(area, "yuenan_vietnammm_area");
            log.info("当前地区已爬完,下一个地区");
        }

    }

    public void crawDetailPage() throws Exception{

        WebDriver webDriver = MySeleniumUtils.getWebDriver();

        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "yuenan_vietnammm_restaurants");

        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(2000);

            try {
                WebElement button = webDriver.findElement(By.xpath("//*[@id=\"tab_MoreInfo\"]/a"));
                button.click();
                Thread.sleep(2000);

                document.put("address",webDriver.findElement(By.xpath("//*[@class=\"card-body\"]")).getText().trim());

            }catch (Exception e){

            }

            document.put("update_time", LocalDateTime.now());
            document.put("is_craw", true);
            mongoTemplate.save(document, "yuenan_vietnammm_restaurants");
            log.info("当前详情页数据保存完毕");

        }

    }


}
