package com.bin.meishikecan.ThailandSite;

import com.bin.meishikecan.utils.MySeleniumUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class SkyscannerCrawService {
    private String url = "https://www.skyscanner.co.th/hotels/search?entity_id=27536671";

    @Autowired
    private MongoTemplate mongoTemplate;

    public void crawListPage() throws Exception{
        WebDriver webDriver = MySeleniumUtils.getWebDriver();
        webDriver.get(url);
        Thread.sleep(10000);
        List<Document> citys = mongoTemplate.findAll(Document.class, "taiguo_city");
        //关键字
        for (Document dity : citys) {

        }
    }

}
