package com.bin.meishikecan.controller;

import com.bin.meishikecan.common.Constants;
import com.bin.meishikecan.common.ReturnJson;
import com.bin.meishikecan.entity.Demo;
import com.bin.meishikecan.entity.SingletonThread;
import com.bin.meishikecan.error.ReturnErrorJsonHandler;
import com.bin.meishikecan.service.DemoService;
import com.bin.meishikecan.service.RabbitMQService;
import com.bin.meishikecan.service.RedisService;
import com.bin.meishikecan.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;

/**
 * 基本测试
 */
@RestController
@Slf4j
public class DemoController {

    @Autowired
    private DemoService demoService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RabbitMQService rabbitMQService;





    /**
     * springboot servlet测试
     * @return
     */
    @GetMapping("/hello")
    public String hello(){
        for(int i=0;i<1000;i++){
            System.out.println("111");
        }

        return "hello springboot";
    }

    /**
     * 数据库连接测试
     * @return
     */
    @GetMapping("/demo")
    public String demo(){
        List<Demo> all = demoService.findAll();
        System.out.println("调用成功");
        return ReturnJson.success(all);
    }

    /**
     * 单例模式线程安全问题测试
     * @return
     */
    @GetMapping("/singletonDemo")
    public String singletonDemo(){
        SingletonThread run=new SingletonThread();
        Thread thread1 =new Thread(run);
        Thread thread2 =new Thread(run);
        thread1.start();
        thread2.start();
        return ReturnJson.success();
    }

    /**
     * 集成redis测试
     * @param key
     * @return
     */
    @GetMapping("/getRedisDemo")
    public String getRedisDemo(String key){
        if (StringUtil.isEmpty(key)){
            return ReturnJson.error(Constants.isNotNull);
        }
        return ReturnJson.success(redisService.get(key));
    }

    /**
     * 集成redis测试
     * @param key
     * @param value
     * @param expire
     * @return
     */
    @GetMapping("/setRedisDemo")
    public String setRedisDemo(String key,String value,Long expire){
        if (StringUtil.isEmpty(key)||StringUtil.isEmpty(value)){
            return ReturnJson.error(Constants.isNotNull);
        }
        return ReturnJson.success(redisService.save(key,value,expire));
    }

    /**
     * log4j2测试
     */
    @GetMapping("/getLog")
    public String getLog(){
        System.out.println("打印日志");
        log.info("info");
        log.warn("warn");
        log.error("error");
        log.debug("debug");
        return ReturnJson.success();
    }


    /**
     *
     * rabbitmq DirectExchange测试
     */
    @GetMapping("/sendDirectMessage")
    public String sendDirectMessage(){
        rabbitMQService.sendDirectMessage();
        return ReturnJson.success();
    }

    /**
     *
     * rabbitmq TopicExchange测试
     */
    @GetMapping("/sendTopicMessage")
    public String sendTopicMessage(Integer a){
        if (a==1){
            rabbitMQService.sendTopicMessage1();
        }else{
            rabbitMQService.sendTopicMessage2();
        }
        return ReturnJson.success();
    }

    /**
     *
     * rabbitmq TopicExchange测试
     */
    @GetMapping("/sendFanoutMessage")
    public String sendFanoutMessage(){
        rabbitMQService.sendFanoutMessage();
        return ReturnJson.success();
    }

    /**
     *
     * restTemplate测试
     */
    @PostMapping("/getWishData")
    public String getWishData(String token,String id,String name){

        RestTemplate restTemplate=new RestTemplate();
        restTemplate.setErrorHandler(new ReturnErrorJsonHandler());
        String url=String.format("https://merchant.wish.com/api/v2/product/update?access_token=%s&id=%s&name=%s",token,id,name);
        System.out.println(url);
        try {
            String response = restTemplate.getForObject(url, String.class);
            return response;
        }catch (Exception e){
            return e.getMessage();
        }

    }


//TODO 这里lucene的包和es的包中的lucene依赖冲突，暂注释
    /**
     * 1.lucene创建索引
     *
     */
//    @GetMapping("/createIndex")
//    public String luceneTest1() throws Exception{
//        //指定索引库存放的路径
//        Directory directory = FSDirectory.open(new File("D:\\学习资料\\index").toPath());
//        //索引库还可以存放到内存中
//        //Directory directory = new RAMDirectory();
//        //创建indexwriterCofig对象
//        IndexWriterConfig config = new IndexWriterConfig();
//        //创建indexwriter对象
//        IndexWriter indexWriter = new IndexWriter(directory, config);
//        //原始文档的路径
//        File dir = new File("D:\\学习资料\\软件开发笔记\\全文检索\\资料\\searchsource");
//        for (File f : dir.listFiles()) {
//            //文件名
//            String fileName = f.getName();
//            //文件内容
//            String fileContent = FileUtils.readFileToString(f);
//            //文件路径
//            String filePath = f.getPath();
//            //文件的大小
//            long fileSize  = FileUtils.sizeOf(f);
//            //创建文件名域
//            //第一个参数：域的名称
//            //第二个参数：域的内容
//            //第三个参数：是否存储
//            Field fileNameField = new TextField("filename", fileName, Field.Store.YES);
//            //文件内容域
//            Field fileContentField = new TextField("content", fileContent, Field.Store.YES);
//            //文件路径域（不分析、不索引、只存储）
//            Field filePathField = new TextField("path", filePath, Field.Store.YES);
//            //文件大小域
//            Field fileSizeField = new TextField("size", fileSize + "", Field.Store.YES);
//            //创建document对象
//            Document document = new Document();
//            document.add(fileNameField);
//            document.add(fileContentField);
//            document.add(filePathField);
//            document.add(fileSizeField);
//            //创建索引，并写入索引库
//            indexWriter.addDocument(document);
//        }
//        //关闭indexwriter
//        indexWriter.close();
//        return "OK";
//
//    }


    /**
     * ..lucene查询索引
     *
     */
//    @GetMapping("/searchIndex")
//    public String searchIndex() throws Exception{
//        //指定索引库存放的路径
//        Directory directory = FSDirectory.open(new File("D:\\学习资料\\index").toPath());
//        //创建indexReader对象
//        IndexReader indexReader = DirectoryReader.open(directory);
//        //创建indexsearcher对象
//        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
//        //创建查询
//        Query query = new TermQuery(new Term("filename", "apache"));
//        //执行查询
//        //第一个参数是查询对象，第二个参数是查询结果返回的最大值
//        TopDocs topDocs = indexSearcher.search(query, 10);
//        //查询结果的总条数
//        System.out.println("查询结果的总条数："+ topDocs.totalHits);
//        //遍历查询结果
//        //topDocs.scoreDocs存储了document对象的id
//        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
//            //scoreDoc.doc属性就是document对象的id
//            //根据document的id找到document对象
//            Document document = indexSearcher.doc(scoreDoc.doc);
//            System.out.println(document.get("filename"));
//            //System.out.println(document.get("content"));
//            System.out.println(document.get("path"));
//            System.out.println(document.get("size"));
//            System.out.println("-------------------------");
//        }
//        //关闭indexreader对象
//        indexReader.close();
//        return "OK";
//    }


//    /**
//     * lucene分析器
//     * @throws Exception
//     */
//    @GetMapping("testTokenStream")
//    public void testTokenStream() throws Exception {
//        //创建一个标准分析器对象
//        Analyzer analyzer = new StandardAnalyzer();
//        //获得tokenStream对象
//        //第一个参数：域名，可以随便给一个
//        //第二个参数：要分析的文本内容
//        TokenStream tokenStream = analyzer.tokenStream("test", "The Spring Framework provides a comprehensive programming and configuration model.");
//        //添加一个引用，可以获得每个关键词
//        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
//        //添加一个偏移量的引用，记录了关键词的开始位置以及结束位置
//        OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
//        //将指针调整到列表的头部
//        tokenStream.reset();
//        //遍历关键词列表，通过incrementToken方法判断列表是否结束
//        while(tokenStream.incrementToken()) {
//           //取关键词
//            System.out.println(charTermAttribute);
//        }
//        tokenStream.close();
//    }





}
