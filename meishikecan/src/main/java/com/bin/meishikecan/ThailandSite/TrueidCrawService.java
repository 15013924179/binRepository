package com.bin.meishikecan.ThailandSite;

import com.bin.meishikecan.utils.MySeleniumUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class TrueidCrawService {
    private static String url = "https://travel.trueid.net";

    @Autowired
    private MongoTemplate mongoTemplate;

    public void crawListPage() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        webDriver.get(url);
        Thread.sleep(2000);
        //打开分类菜单
        WebElement categoryButton = webDriver.findElement(By.xpath("//*[@class=\"_button__SelectProvinceButton-sc-1kccepx-2 iWGehu button\"]"));
        categoryButton.click();
        Thread.sleep(1000);
        //获取所有地点分类
        List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"style__ProvinceListsBox-tu2en6-0 style-tu2en6-2 iWLXpf\"]//a[not (contains(@class,\"button\"))]"));
        List<String> urls = new ArrayList<>();
        for (WebElement webElement : elements) {
            String categoryUrl = webElement.getAttribute("href");
            urls.add(categoryUrl);
        }
        int index = 1;
        log.info("初始化结束，开始爬取列表页：https://travel.trueid.net");
        //遍历地点的类别url
        for (String url : urls) {
            if (url == null || url == "") {
                continue;
            }
            log.info("开始访问第" + index + "个目的地url");
            webDriver.get(url);
            Thread.sleep(2000);
            Actions actions = new Actions(webDriver);
            //获取点击更多按钮（可能包含1-3个按钮）并且点击
            while (true) {
                List<WebElement> buttons = webDriver.findElements(By.xpath("//*[@class=\"global__ViewMoreButton-sc-10c7lju-8 eOPvEl\"]"));
                if (buttons == null || buttons.size() == 0) {
                    break;
                }

                for (WebElement button : buttons) {
                    try {
                        actions.moveToElement(button).click().perform();
                        Thread.sleep(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            //获取三大板块
            List<WebElement> sections = webDriver.findElements(By.xpath("//*[@class=\"main-section\"]/div[3]/section"));
            for (WebElement section : sections) {
                String feild = section.findElement(By.xpath("./header")).getText();
                if ("ที่เที่ยว".equals(feild)) {
                    log.info("开始爬取景区模块");
                    saveMode(section, "trueid_travel");
                    log.info("景区模块爬取完毕");
                }
                if ("ร้านอาหาร".equals(feild)) {
                    log.info("开始爬取餐厅模块");
                    saveMode(section, "trueid_restaurant");
                    log.info("餐厅模块爬取完毕");
                }
                if ("ที่พัก".equals(feild)) {
                    log.info("开始爬取住所模块");
                    saveMode(section, "trueid_hotel");
                    log.info("住所模块爬取完毕");
                }
            }

            log.info("第" + index + "个目的地url列表页爬取完毕");
            index++;
        }
        log.info("列表页爬取完毕");
    }

    /**
     * 保存单一模块数据
     *
     * @param mode
     * @param tableName
     */
    public void saveMode(WebElement mode, String tableName) {
        List<WebElement> data = mode.findElements(By.xpath("./div/article/div"));
        String feild = mode.findElement(By.xpath("./header")).getText();
        for (WebElement webElement : data) {
            Document document = new Document();

            Optional.ofNullable(webElement.findElement(By.xpath("./a")))
                    .map(x -> {
                        return x.getAttribute("href");
                    })
                    .filter(x -> !x.isEmpty())
                    .ifPresent(x -> {
                        document.put("url", x);
                    });
            //判断是否爬取过该列表项
            String oldUrl = (String) document.get("url");
            boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, tableName);
            if (exists) {
                log.info("该列表项已经爬取过");
                continue;
            }

            Optional.ofNullable(webElement.findElement(By.xpath("./div/div[1]")))
                    .map(WebElement::getText)
                    .map(String::trim)
                    .ifPresent(x -> {
                        document.put("title", x);
                    });

            //保存目的地类型
            Optional.ofNullable(webElement.findElement(By.xpath("./div/b")))
                    .map(WebElement::getText)
                    .map(x -> {
                        return x.replace("#", "");
                    })
                    .map(String::trim)
                    .ifPresent(x -> {
                        document.put("place", x);
                    });

            document.put("felid", feild);
            document.put("create_time", LocalDateTime.now());
            document.put("update_time", LocalDateTime.now());
            document.put("is_craw", false);
            mongoTemplate.save(document, tableName);
        }
    }


    public void TypeDetail(String tableName){
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        List<Document> list = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, tableName);
        try {
            log.info("开始爬取：" + tableName);
            int index = 1;
            Pattern pattern = Pattern.compile("[0-9]+");
            for (Document document : list) {
                webDriver.get((String) document.get("url"));
                Thread.sleep(2000);

                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"style__ContentDetailBox-sc-150i3lj-0 style-sc-150i3lj-1 bietjr\"]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("content", x);
                        });

                //保存浏览量
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"headmanage\"]/div[3]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("brower", x);
                        });

                //点赞数
                try {
                    Optional.ofNullable(webDriver.findElement(By.xpath("//*[@class=\"headmanage\"]/div[4]/span")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("like", x);
                            });
                }catch (Exception e){
                    document.put("like", "0");
                }


                //评论数
                try {
                    webDriver.switchTo().frame(webDriver.findElement(By.xpath("//*[@id=\"__next\"]/main/div[3]/div/div[1]/div/div/div/div/span/iframe")));
                    Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"_50f7\")]")))
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
                                document.put("comment_number", x);
                            });
                } catch (Exception e) {
                    document.put("comment_number", "0");
                }

                document.put("update_time", LocalDateTime.now());
                document.put("is_craw", true);
                mongoTemplate.save(document, tableName);
                log.info(tableName + "爬取第" + index + "个完成");
                index++;
            }
            log.info("爬取完毕：" + tableName);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("出现异常：" + tableName);
        } finally {
            webDriver.close();
        }
    }


}
