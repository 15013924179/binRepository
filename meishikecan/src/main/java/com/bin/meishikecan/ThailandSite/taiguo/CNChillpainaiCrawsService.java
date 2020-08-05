package com.bin.meishikecan.ThailandSite.taiguo;

import com.bin.meishikecan.utils.BaiduTranslate;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Slf4j
public class CNChillpainaiCrawsService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void translate() throws Exception{
        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_zh_translate").is(false)), Document.class, "chillpainai_travel");

        int index = 1;

        log.info("开始翻译");

        for (Document document : documents) {


            long start = System.currentTimeMillis();

            try {
                String title = (String)document.get("title");

                String content = (String)document.get("content");

                document.put("chinese_title", BaiduTranslate.translateText(title,"th","zh"));

                document.put("chinese_content",BaiduTranslate.translateText(content,"th", "zh"));

                document.put("is_zh_translate",true);

                mongoTemplate.save(document,"chillpainai_travel");

                long end = System.currentTimeMillis();

                log.info("翻译完成个数："+index+"|当前行翻译保存完毕，共耗时："+(double)(end - start)/1000+"秒");

                index++;
            }catch (Exception e){
//                e.printStackTrace();
                log.info("翻译异常");
            }

        }

    }


}
