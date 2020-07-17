package com.bin.meishikecan.ThailandSite;

import com.bin.meishikecan.utils.GoogleTranslate;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CNChillpainaiCrawsService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void translate() throws Exception{
        List<Document> documents = mongoTemplate.findAll(Document.class, "chillpainai_travel");


        long start = System.currentTimeMillis();

        int index = 1;

        log.info("开始翻译");

        for (Document document : documents) {
            String title = (String)document.get("title");

            String content = (String)document.get("content");

            document.put("title",GoogleTranslate.translateText(title,"th", "zh-cn"));

            document.put("content",GoogleTranslate.translateText(content,"th", "zh-cn"));

            mongoTemplate.save(document,"cn_chillpainai_travel");

            log.info("翻译完成个数："+index);

            index++;
        }

        long end = System.currentTimeMillis();

        log.info("翻译保存完毕，共耗时："+(double)(end - start)/1000+"秒");
    }
}
