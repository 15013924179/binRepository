package com.bin.meishikecan.reptile;

import com.bin.meishikecan.entity.SanookTravel;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;
import java.util.Objects;


/**
 * 爬取https://www.sanook.com/
 */
@Service
public class SanookReptile {

    @Autowired
    private MongoTemplate mongoTemplate;

    //获取列表页
    public void reptileListPage() throws Exception {
        System.setProperty("webdriver.chrome.driver", "D:\\chromedriver.exe");
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.get("https://www.sanook.com");
        Thread.sleep(2000);
        //选择旅游
        WebElement travelButton = chromeDriver.findElementByXPath("//*[@id=\"__next\"]/div/div[1]/div/nav/ul/li[11]");
        travelButton.click();
        Thread.sleep(2000);
        //菜单选择
        WebElement elementByXPath = chromeDriver.findElementByXPath("//*[@id=\"__next\"]/div/div[1]/div/div/div/div/span[1]/a");
        elementByXPath.click();
        Thread.sleep(2000);
        //循环点击下一页
        int index = 0;
        while (index < 10) {
            WebElement pageButton = chromeDriver.findElementByXPath("//*[@id=\"__next\"]/div/div[2]/div/div/div/div[2]/button");
            if (pageButton == null) {
                break;
            }
            pageButton.click();
            Thread.sleep(1000);
            index++;
        }
        //所有列表项
        List<WebElement> list = chromeDriver.findElementsByXPath("//*[@id=\"__next\"]/div/div[2]/div/div/div/div[2]/div[4]/div/div");
        for (WebElement webElement : list) {
            WebElement element = webElement.findElement(By.xpath("./article/div[2]/div/h3/span/a"));
            String title = element.getAttribute("title");
            String href = element.getAttribute("href");
            SanookTravel sanookTravel = new SanookTravel();
            sanookTravel.setTitle(title);
            sanookTravel.setUrl(href);
            mongoTemplate.insert(sanookTravel, "sanook_travel");
        }
        chromeDriver.close();

    }

    //获取详情页
    public void reptileDetailPage() throws Exception {
        List<SanookTravel> list = mongoTemplate.findAll(SanookTravel.class, "sanook_travel");
        System.setProperty("webdriver.chrome.driver", "D:\\chromedriver.exe");
        // 设置浏览器隐藏
        ChromeOptions chromeOptions = new ChromeOptions();
//      chromeOptions.addArguments("--no-sandbox");
//      chromeOptions.addArguments("--headless");
//      chromeOptions.addArguments("--disable-dev-shm-usage");
        ChromeDriver chromeDriver = new ChromeDriver(chromeOptions);
        for (SanookTravel sanookTravel : list){
            String url = sanookTravel.getUrl();
            chromeDriver.get(url);
            Thread.sleep(1000);
            //评论数
            String commentNumber = chromeDriver.findElementByXPath("//*[contains(@class,'jsx-2376132709 count')]").getText();
            sanookTravel.setCommentNumber(commentNumber == "" ? 0 : Long.parseLong(commentNumber));
            //时间
            String datatime = chromeDriver.findElementByXPath("//*[contains(@class,'jsx-2376132709 infoItem')]/time").getAttribute("datetime");
            sanookTravel.setDataTime(datatime);
            //内容
            String content = chromeDriver.findElementByXPath("//*[contains(@class,'EntryContent')]/article/div[2]").getText();
            sanookTravel.setContent(content);
            //图片
            String img=chromeDriver.findElementByXPath("//*[contains(@class,'jsx-2954975791 thumbnail')]/img").getAttribute("src");
            sanookTravel.setImg(img);
            sanookTravel.setSiteType("sanook");
            sanookTravel.setCrawlStatus("YES");
            mongoTemplate.save(sanookTravel,"sanook_travel");

        }
        chromeDriver.close();
    }
}
