package com.bin.meishikecan.common;

import com.bin.meishikecan.utils.SpringApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.Iterator;
import java.util.Map;

@Slf4j
public class MongodbPipeline implements Pipeline {

    private MongoTemplate mongoTemplate;

    public MongodbPipeline () {
        //从spring获取MongoTemplate
        this.mongoTemplate = (MongoTemplate) SpringApplicationContextHolder.getBean(MongoTemplate.class);
    }

    @Override
    public void process(ResultItems resultItems, Task task) {

        Map<String, Object> mapResults = resultItems.getAll();

        if (mapResults == null || mapResults.size() == 0) {
            return;
        }

        Iterator<Map.Entry<String, Object>> iter = mapResults.entrySet().iterator();

        Map.Entry<String, Object> entry;

        Document document = new Document();

        while (iter.hasNext()) {

            entry = iter.next();

            System.out.println();

            document.put(entry.getKey(),entry.getValue());

        }

        mongoTemplate.insert(document, "jd_iphone11");

        log.info("插入一条数据成功");

    }
}
