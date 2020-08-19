package com.bin.meishikecan.ThailandSite;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bin.meishikecan.ThailandSite.malaixiya.*;
import com.bin.meishikecan.utils.ChaoJiYing;
import com.bin.meishikecan.utils.MySeleniumUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


@SpringBootTest
public class MalaixiyaCrawServiceTest {

    @Resource
    private MalaixiyaTripadvisorCrawsService malaixiyaTripadvisorCrawsService;

    @Autowired
    private MalaixiyaMaFengWoCrawsService malaixiyaMaFengWoCrawsService;

    @Autowired
    private MalaixiyaBookingCrawsService malaixiyaBookingCrawsService;

    @Autowired
    private MalaixiyaCtripCrawService malaixiyaCtripCrawService;

    @Autowired
    private MalaixiyaQyerCrawsService malaixiyaQyerCrawsService;

    @Test
    public void  crawTripadvisor() throws Exception{
        malaixiyaTripadvisorCrawsService.crawDetailPageRestan();
    }

    @Test
    public void  crawTripadvisor1() throws Exception{
        malaixiyaTripadvisorCrawsService.crawListPageRes();
    }

    @Test
    public void  crawTripadvisor2() throws Exception{
        malaixiyaTripadvisorCrawsService.crawListPageTravel();
    }

    @Test
    public void  crawMaFengWo() throws Exception{
        WebDriver webDriver = MySeleniumUtils.getWebDriverHavingImg();
        webDriver.get("https://www.mafengwo.cn/hotel/11049/");

        WebElement element = webDriver.findElement(By.xpath("//*[@id=\"captcha-img\"]"));

        String path = "C:\\Users\\k\\Desktop\\picture.jpg";

        screenShotForElement(webDriver,element,path);

        String s = ChaoJiYing.PostPic("15013924179", "1314520", "907156", "1004", "0", path);

        JSONObject jsonObject = JSON.parseObject(s);

        String code = jsonObject.getString("pic_str");

        Thread.sleep(3000);

        WebElement input = webDriver.findElement(By.xpath("//*[@id=\"captcha-input\"]"));

        input.sendKeys(code);

        Thread.sleep(2000);

        WebElement button = webDriver.findElement(By.xpath("//*[@class=\"dialog-confirm-btn\"]"));

        button.click();
    }

    //解析网页的图片并且保存到本地
    public static void screenShotForElement(WebDriver driver,
                                            WebElement element, String path) throws InterruptedException {
        File scrFile = ((TakesScreenshot) driver)
                .getScreenshotAs(OutputType.FILE);
        try {
            Point p = element.getLocation();
            int width = element.getSize().getWidth();
            int height = element.getSize().getHeight();
            Rectangle rect = new Rectangle(width, height);
            BufferedImage img = ImageIO.read(scrFile);
            BufferedImage dest = img.getSubimage(p.getX(), p.getY(),
                    rect.width, rect.height);
            ImageIO.write(dest, "png", scrFile);
            Thread.sleep(1000);
            FileUtils.copyFile(scrFile, new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void  crawMaFengW1o() throws Exception{
        malaixiyaMaFengWoCrawsService.crawDetailPageHotel();
    }

    @Test
    public void  crawMaFengWo1() throws Exception{
        malaixiyaMaFengWoCrawsService.crawDetailPageTravel();
    }

    @Test
    public void  crawBooking() throws Exception{
        malaixiyaBookingCrawsService.crawDetailPage();
    }

    @Test
    public void  crawCtrip() throws Exception{
        malaixiyaCtripCrawService.crawDetailPageTravel();
    }

    @Test
    public void  crawCtrip1() throws Exception{
        malaixiyaCtripCrawService.crawListPageHotel();
    }

    @Test
    public void  crawCtrip2() throws Exception{
        malaixiyaCtripCrawService.crawsListPageByRes();
    }


    @Test
    public void  crawQyer() throws Exception{
        malaixiyaQyerCrawsService.crawDetailPageTravel();
    }









}
