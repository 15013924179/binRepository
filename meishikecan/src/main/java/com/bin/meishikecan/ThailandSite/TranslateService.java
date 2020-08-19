package com.bin.meishikecan.ThailandSite;


import com.bin.meishikecan.utils.GoogleTranslate;
import com.bin.meishikecan.utils.MyThreadUtils;
import com.bin.meishikecan.utils.ProxyHttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class TranslateService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void translate (String table,List<String> fields,String translateFrom,String translateTo) throws Exception{
        MyThreadUtils.THREAD_LOCAL.set(ProxyHttpUtils.getipAndPort());

        List<Document> documents = mongoTemplate.find(new Query(Criteria.where("is_translate").is(false)), Document.class, table);

        log.info("开始翻译");

        int index=1;

        for (Document document : documents) {

            long start = System.currentTimeMillis();

            try {

                for (String field : fields) {
                    String text = (String)document.get(field);

                    document.put("cn_"+field, GoogleTranslate.translateText(text,translateFrom, translateTo));
                }

                document.put("is_translate",true);

                mongoTemplate.save(document,table);

                long end = System.currentTimeMillis();

                log.info("翻译完成个数："+index+"|当前行翻译保存完毕，共耗时："+(double)(end - start)/1000+"秒");

                index++;
            }catch (Exception e){
                log.info("翻译异常,重新设置代理ip");

                Thread.sleep(2000);

                MyThreadUtils.THREAD_LOCAL.set(ProxyHttpUtils.getipAndPort());

            }

        }


    }
}
