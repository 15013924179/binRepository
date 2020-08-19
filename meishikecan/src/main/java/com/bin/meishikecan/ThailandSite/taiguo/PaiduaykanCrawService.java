package com.bin.meishikecan.ThailandSite.taiguo;

import com.bin.meishikecan.utils.GoogleTranslate;
import com.bin.meishikecan.utils.MySeleniumUtils;
import com.bin.meishikecan.utils.MyThreadUtils;
import com.bin.meishikecan.utils.ProxyHttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PaiduaykanCrawService {

    private String url ="https://www.paiduaykan.com/travel/category/travel/paiduaykantravel/page/";

    @Autowired
    private MongoTemplate mongoTemplate;

    public void crawListPage(){
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        //最大页数
        int maxPage = 18;
        //当前页
        int page = 1;
        while (page <= 18) {
            webDriver.get(url+page);
            log.info("爬取第" + page + "页");
            //列表项
            List<WebElement> elements = webDriver.findElements(By.xpath("//*[@class=\"col-lg-3 col-md-4 col-xs-6 thumb\"]"));

            for (WebElement webElement : elements) {
                Document document = new Document();

                //url
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
                boolean exists = mongoTemplate.exists(new Query(Criteria.where("url").is(oldUrl)), Document.class, "paiduaykan_travel");
                if (exists) {
                    log.info("该列表项已经爬取过");
                    continue;
                }

                //名称
                Optional.ofNullable(webElement.findElement(By.xpath("./a")))
                        .map(x -> {
                            return x.getAttribute("title");
                        })
                        .map(String::trim)
                        .ifPresent(x -> {
                            document.put("title", x);
                        });


                document.put("create_time", LocalDateTime.now());
                document.put("update_time", LocalDateTime.now());
                document.put("is_craw", false);
                mongoTemplate.save(document, "paiduaykan_travel");
            }
            page++;
        }
    }

    public void crawDetailPage() throws Exception {
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        log.info("开始爬取详情页");
        Pattern pattern = Pattern.compile("[0-9|,]+");
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_craw").is(false)), Document.class, "paiduaykan_travel");
        for (Document document : documents) {
            webDriver.get((String) document.get("url"));
            Thread.sleep(2000);

            //保存内容
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[@id=\"boxall\"]/div[2]")))
                        .map(WebElement::getText)
                        .map(String::trim)
                        .filter(x -> !x.isEmpty())
                        .ifPresent(x -> {
                            document.put("content", x);
                        });
            } catch (Exception e) {

            }


            //保存浏览量
            try {
                Optional.ofNullable(webDriver.findElement(By.xpath("//*[contains(@class,\"article-info pull-right\")]")))
                        .map(WebElement::getText)
                        .map(x -> {
                            return x.split("\\|")[1];
                        })
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
                            document.put("brower_number", x);
                        });
            } catch (Exception e) {
                document.put("brower_number", "0");
            }


            document.put("update_time", LocalDateTime.now());
            document.put("is_craw", true);
            mongoTemplate.save(document, "paiduaykan_travel");
            log.info("当前详情页数据保存完毕");
        }
        log.info("详情页数据抓取完毕");
    }

    public void translate(){

        MyThreadUtils.THREAD_LOCAL.set(ProxyHttpUtils.getipAndPort());

        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_translate").is(false)), Document.class, "paiduaykan_travel");

        log.info("开始翻译");

        int index=1;

        for (Document document : documents) {

            long start = System.currentTimeMillis();

            try {

                String title = (String)document.get("title");

                document.put("cn_title", GoogleTranslate.translateText(title,"th", "zh"));

                String content = (String)document.get("content");

                document.put("cn_content", GoogleTranslate.translateText(content,"th", "zh"));

                document.put("is_translate",true);

                mongoTemplate.save(document,"paiduaykan_travel");

                long end = System.currentTimeMillis();

                log.info("翻译完成个数："+index+"|当前行翻译保存完毕，共耗时："+(double)(end - start)/1000+"秒");

                index++;
            }catch (Exception e){
                log.info("翻译异常,重新设置代理ip");

                MyThreadUtils.THREAD_LOCAL.set(ProxyHttpUtils.getipAndPort());

            }

        }

    }
}
