package com.bin.meishikecan.ThailandSite;

import com.bin.meishikecan.utils.MySeleniumUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
public class WongnaiCrawService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     *
     * @param url   站点
     * @param table  表名
     * @param index  第几个地点
     * @param page   页数
     * @throws Exception
     */
    public void crawListPage(String url, String table,Integer index,Integer page) throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriverHavingImg();
        webDriver.get(url);
        //等待手动验证
        Thread.sleep(5000);
        Actions actions = new Actions(webDriver);
        //防止弹窗
        try {
            WebElement btn = webDriver.findElement(By.xpath("//*[@class=\"sc-fznxsB gIIAsV\"]"));
            btn.click();
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //获取所有地区按钮
        WebElement button = webDriver.findElement(By.xpath("//*[@class=\"sc-1woz07j-0 hvuhEN\"]"));
        button.click();
        Thread.sleep(1000);
        //所有地区
        List<WebElement> places = webDriver.findElements(By.xpath("//*[@class=\"sc-996lfo-3 hcLdCv\"]/div/div"));
        log.info("初始化完毕，开始爬取列表页");
        //可输入参数：index ： 第几个地点类型
        //          page ： 页数
        if (index == null){
            index = 1;
        }
        if (page == null){
            page = 1;
        }

        //循环爬取地点
        while (index <= places.size()) {
            webDriver.get(url+"&page.number="+page);
            button = webDriver.findElement(By.xpath("//*[@class=\"sc-1woz07j-0 hvuhEN\"]"));
            button.click();
            Thread.sleep(3000);
            //所有地区
            WebElement place = webDriver.findElement(By.xpath("//*[@class=\"sc-996lfo-3 hcLdCv\"]/div/div[" + index + "]"));
            String placeString = place.getText();
            actions.moveToElement(place).click().perform();
            Thread.sleep(5000);
            log.info("开始爬取第" + index + "个地点");

            while (true) {
                log.info("开始爬取第" + page + "页列表项数据");
                //获取当前页面的列表项
                List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"sc-10ino0a-13 dKFLtJ\"]"));

                for (WebElement element : elements) {
                    Document document = new Document();
                    //保存标题
                    Optional.ofNullable(element.findElement(By.xpath("./div/*[1]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("title", x);
                            });
                    //保存url
                    Optional.ofNullable(element.getAttribute("href"))
                            .map(x -> {return x.split("\\?")[0];})
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("url", x);
                            });
                    //判断是否爬取过该列表项
                    String oldUrl = (String) document.get("url");
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, table);
                    if (exists) {
                        log.info("已爬取过该列表项，跳过");
                        continue;
                    }
                    document.put("place_type",placeString);
                    document.put("is_craw", false);
                    document.put("create_time", LocalDateTime.now());
                    document.put("update_time", LocalDateTime.now());
                    mongoTemplate.save(document, table);
                }

                //翻页
                try {
                    WebElement pageButton = webDriver.findElement(By.xpath("//*[contains(@class,\"sc-fznxsB fieqCO\")][text()=\"ถัดไป >\"]"));
                    ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView();", pageButton);
                    Thread.sleep(2000);
                    actions.moveToElement(pageButton).click().perform();
                    Thread.sleep(10000);
                } catch (Exception e) {
                    log.info("已经最后一页了");
                    page = 1;
                    break;
                }
                page++;  //页数

            }
            index++;  //第几个地点

        }
        log.info("列表页爬取完毕");
    }


    /**
     * 旅游专用
     * @param url   站点
     * @param table  表名
     * @param index  第几个地点
     * @param page   页数
     * @throws Exception
     */
    public void crawTravelListPage(String url, String table,Integer index,Integer page) throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriverHavingImg();
        webDriver.get(url);
        //等待手动验证
        Thread.sleep(5000);
        Actions actions = new Actions(webDriver);
        //防止弹窗
        try {
            WebElement btn = webDriver.findElement(By.xpath("//*[@class=\"sc-fznxsB gIIAsV\"]"));
            btn.click();
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //获取所有地区按钮
        WebElement button = webDriver.findElement(By.xpath("//*[@class=\"sc-1woz07j-0 hvuhEN\"]"));
        button.click();
        Thread.sleep(3000);
        //所有地区
        List<WebElement> places = webDriver.findElements(By.xpath("//*[@class=\"sc-996lfo-3 hcLdCv\"]/div/div"));
        log.info("初始化完毕，开始爬取列表页");
        //可输入参数：index ： 第几个地点类型
        //          page ： 页数
        if (index == null){
            index = 1;
        }
        if (page == null){
            page = 1;
        }

        //循环爬取地点
        while (index <= places.size()) {
            webDriver.get(url+"&page.number="+page);
            button = webDriver.findElement(By.xpath("//*[@class=\"sc-1woz07j-0 hvuhEN\"]"));
            button.click();
            Thread.sleep(3000);
            //所有地区
            WebElement place = webDriver.findElement(By.xpath("//*[@class=\"sc-996lfo-3 hcLdCv\"]/div/div[" + index + "]"));
            String placeString = place.getText();
            actions.moveToElement(place).click().perform();
            Thread.sleep(5000);
            log.info("开始爬取第" + index + "个地点");

            while (true) {
                log.info("开始爬取第" + page + "页列表项数据");
                //获取当前页面的列表项
                List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"sc-7z7jpy-3 hYHNzB\"]"));

                for (WebElement element : elements) {
                    Document document = new Document();
                    //保存标题
                    Optional.ofNullable(element.findElement(By.xpath("./*[1]")))
                            .map(WebElement::getText)
                            .map(String::trim)
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("title", x);
                            });
                    //保存url
                    Optional.ofNullable(element.getAttribute("href"))
                            .map(x -> {return x.split("\\?")[0];})
                            .filter(x -> !x.isEmpty())
                            .ifPresent(x -> {
                                document.put("url", x);
                            });
                    //判断是否爬取过该列表项
                    String oldUrl = (String) document.get("url");
                    boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, table);
                    if (exists) {
                        log.info("已爬取过该列表项，跳过");
                        continue;
                    }
                    document.put("place_type",placeString);
                    document.put("is_craw", false);
                    document.put("create_time", LocalDateTime.now());
                    document.put("update_time", LocalDateTime.now());
                    mongoTemplate.save(document, table);
                }

                //翻页
                try {
                    WebElement pageButton = webDriver.findElement(By.xpath("//*[contains(@class,\"sc-fznxsB fieqCO\")][text()=\"ถัดไป >\"]"));
                    ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView();", pageButton);
                    Thread.sleep(2000);
                    actions.moveToElement(pageButton).click().perform();
                    Thread.sleep(10000);
                } catch (Exception e) {
                    log.info("已经最后一页了");
                    page = 1;
                    break;
                }
                page++;  //页数

            }
            index++;  //第几个地点

        }
        log.info("列表页爬取完毕");
    }

    public void crawDetailPage(String table) throws Exception{
        WebDriver webDriver = MySeleniumUtils.getWebDriverHavingImg();
        log.info("开始爬取详情页");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, table);
        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(5000);

            //保存地址
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"sc-1xjzh3o-11 cCkZvK\")]")))
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
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"hfew2f-1 jukOhp\")]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("contact", x);
                        });
            } catch (Exception e) {

            }

            //保存价格
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"_2qDKIyMmA-jMRyfxACZWt6\")]/../span[2]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("total", x);
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




}
