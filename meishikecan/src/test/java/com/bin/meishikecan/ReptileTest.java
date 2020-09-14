package com.bin.meishikecan;

import com.bin.meishikecan.common.JDPageProcessor;
import com.bin.meishikecan.common.MongodbPipeline;
import com.bin.meishikecan.reptile.SanookReptile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.pipeline.ConsolePipeline;

@SpringBootTest
public class ReptileTest {

    @Autowired
    private SanookReptile sanookReptile;

    @Test
    void replite1() throws Exception{
        sanookReptile.reptileListPage();
    }

    @Test
    void replite2() throws Exception{
        sanookReptile.reptileDetailPage();
    }

    @Test
    void jdreplite() throws Exception {
        System.setProperty("selenuim_config", "D:\\config.ini");
        Spider.create(new JDPageProcessor()).addUrl("https://search.jd.com/Search?keyword=iphone11&suggest=1.his.0.0&wq=iphone11&page=1&s=1&click=0")
                .setDownloader(new SeleniumDownloader("D:\\chromedriver.exe"))
                .addPipeline(new MongodbPipeline())
                .addPipeline(new ConsolePipeline()).thread(1).run();
    }

}
