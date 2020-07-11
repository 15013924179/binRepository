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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class WongnaiCrawService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * @param url   站点
     * @param table 表名
     * @param index 第几个地点
     * @param page  页数
     * @throws Exception
     */
    public void crawListPage(String url, String table, Integer index, Integer page) throws Exception {
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
        if (index == null) {
            index = 1;
        }
        if (page == null) {
            page = 1;
        }

        //循环爬取地点
        while (index <= places.size()) {
            webDriver.get(url + "&page.number=" + page);
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
                            .map(x -> {
                                return x.split("\\?")[0];
                            })
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
                    document.put("place_type", placeString);
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
                    Thread.sleep(2000);
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
     *
     * @param url   站点
     * @param table 表名
     * @param index 第几个地点
     * @param page  页数
     * @throws Exception
     */
    public void crawTravelListPage(String url, String table, Integer index, Integer page) throws Exception {
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
        if (index == null) {
            index = 1;
        }
        if (page == null) {
            page = 1;
        }

        //循环爬取地点
        while (index <= places.size()) {
            webDriver.get(url + "&page.number=" + page);
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
                            .map(x -> {
                                return x.split("\\?")[0];
                            })
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
                    document.put("place_type", placeString);
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
                    Thread.sleep(2000);
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

    public void crawDetailPage(String table) throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriverHavingImg();
        log.info("开始爬取详情页");
        Pattern pattern = Pattern.compile("[0-9 |,]+");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, table);
        boolean bool= true;
        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(2000);

            while (true){
                try{
                    WebElement element = webDriver.findElement(By.xpath("//*[text()=\"Please complete this captcha to continue\"]"));
                    log.info("出现反爬虫验证");
                    Thread.sleep(60000);
                }catch (Exception e){
                    log.info("未出现反爬虫验证");
                    break;
                }
            }

            if (bool) {
                //防止弹窗
                try {
                    WebElement btn = webDriver.findElement(By.xpath("//*[@class=\"sc-fznxsB gIIAsV\"]"));
                    btn.click();
                    Thread.sleep(1000);
                    bool = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


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


            //保存价格 旅游
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"sc-159e0rz-0 gQDMID\")]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("total", x);
                        });
            } catch (Exception e) {

            }

            //评论数
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"do39q0-0 fxQZvy sc-1afxgci-2 fkyxau\")]/a/div/div/h2")))
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

            //评分数
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"do39q0-0 fxQZvy sc-1afxgci-2 fkyxau\")]/a/div/div/div")))
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

            //排行
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"sc-1afxgci-4 bvEbJw\")]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("rank", x);
                        });
            } catch (Exception e) {

            }

            //介绍
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"x1fflo-1 gzYlCt\")]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("suggest", x);
                        });
            } catch (Exception e) {

            }

            //计算评分
            try{
                //五个等级的评分
                List<WebElement> scores = webDriver.findElements(By.xpath("//*[contains(@class,\"sc-1nastw3-0 bdlWei\")]/div/div[3]"));
                Integer score =0;
                for (int i=0 ;i<scores.size();i++){
                    String s =scores.get(i).getText();
                    score = score + (Integer.parseInt(s) * (5-i));
                    document.put("score_grade_"+(5-i),s);
                }
                String sn = (String) document.get("score_number");
                Integer score_number = Integer.parseInt(sn);
                BigDecimal divide = new BigDecimal(score).divide(new BigDecimal(score_number), 2, BigDecimal.ROUND_HALF_UP);
                document.put("score",divide+"");
            }catch (Exception e){
                document.put("score","0");
            }


//            try {
//                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"_2qDKIyMmA-jMRyfxACZWt6\")]/../span[2]")))
//                        .map(WebElement::getText)
//                        .map(String::trim)
//                        .filter(x -> !x.isEmpty())
//                        .ifPresent(x -> {
//                            document.put("total", x);
//                        });
//            } catch (Exception e) {
//
//            }

            document.put("update_time", LocalDateTime.now());
            document.put("is_craw", true);
            mongoTemplate.save(document, table);
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }


}
