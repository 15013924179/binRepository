package com.bin.meishikecan.controller;

import com.bin.meishikecan.config.HtmlUintUtil;
import com.bin.meishikecan.config.PoolingHttpClientConnectionManagerConfig;
import com.bin.meishikecan.entity.JdItem;
import com.bin.meishikecan.service.JdItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLDocument;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * 爬虫测试
 */
@RestController
@RequestMapping("/reptile")
public class ReptileController {

    @Autowired
    private PoolingHttpClientConnectionManagerConfig httpUtils;
    @Autowired
    private JdItemService jdItemService;

    public static final ObjectMapper MAPPER = new ObjectMapper();


    @GetMapping("/getUrlContent")
    public String getUrlContent() throws Exception {
        CloseableHttpResponse response = null;
        String content = "";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("https://blog.csdn.net");
        try {
            response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                content = EntityUtils.toString(response.getEntity(), "UTF-8");
                System.out.println(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //释放连接
            if (response == null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                httpClient.close();
            }
        }
        return content;
    }

    @GetMapping("/postUrlContent")
    public String postUrlContent() throws Exception {
        //创建HttpClient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建HttpGet请求
        HttpPost httpPost = new HttpPost("https://lol.qq.com/data/info-defail.shtml");
        //声明存放参数的List集合
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("id", "3"));

        //创建表单数据Entity
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params, "UTF-8");

        //设置表单Entity到httpPost请求对象中
        httpPost.setEntity(formEntity);

        CloseableHttpResponse response = null;
        String content = "";
        try {
            //使用HttpClient发起请求
            response = httpClient.execute(httpPost);

            //判断响应状态码是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                //如果为200表示请求成功，获取返回数据
                content = EntityUtils.toString(response.getEntity(), "UTF-8");
                //打印数据长度
                System.out.println(content);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //释放连接
            if (response == null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                httpClient.close();
            }
        }
        return content;

    }

    @GetMapping("/getUrlContentByPool")
    public String getUrlContentByPool() {
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(PoolingHttpClientConnectionManagerConfig.getInstance()).build();
        CloseableHttpResponse response = null;
        String content = "";
        HttpGet httpGet = new HttpGet("http://lol.qq.com/act/a20200612summer/?e_code=507042");
        try {
            response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                content = EntityUtils.toString(response.getEntity(), "UTF-8");
                System.out.println(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //释放连接
            if (response == null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }


    //元素获取
    @GetMapping("/getJsoupUrl")
    public String getJsoupUrl() throws Exception {
        // 解析url地址
        Document document = Jsoup.parse(new URL("http://lol.qq.com/act/a20200612summer/?e_code=507042"), 1000);
        //获取title的内容
        Element title = document.getElementsByTag("title").first();
        System.out.println(title.text());
        return title.text();
    }


    @GetMapping("/init")
    public void process() throws Exception {
        //分析页面发现访问的地址,页码page从1开始，下一页oage加2
        String url = "https://search.jd.com/Search?keyword=%E6%89%8B%E6%9C%BA&enc=utf-8&qrst=1&rt=1&stop=1&vt=2&cid2=653&cid3=655&s=5760&click=0&page=";
        //遍历执行，获取所有的数据
        for (int i = 1; i < 100; i = i + 2) {
            //发起请求进行访问，获取页面数据,先访问第一页
            String html = this.httpUtils.getHtml(url + i);
            //解析页面数据，保存数据到数据库中
            this.parseHtml(html);
        }
        System.out.println("执行完成");
    }

    //解析页面，并把数据保存到数据库中
    private void parseHtml(String html) throws Exception {
        //使用jsoup解析页面
        Document document = Jsoup.parse(html);
        //获取商品数据
        Elements spus = document.select("div#J_goodsList > ul > li");

        //遍历商品spu数据
        for (Element spuEle : spus) {
            //获取商品spu
            Long spuId = spuEle.attr("data-spu") == "" ? 0 : Long.parseLong(spuEle.attr("data-spu"));
            //获取商品sku
            Long skuId = Long.parseLong(spuEle.attr("data-sku"));
            //判断商品是否被抓取过，可以根据sku判断
            Map param = new HashMap<>();
            param.put("skuId", skuId);
            List<JdItem> list = this.jdItemService.findByParam(param);
            //判断是否查询到结果
            if (list.size() > 0) {
                //如果有结果，表示商品已下载，进行下一次遍历
                continue;
            }

            //保存商品数据，声明商品对象
            JdItem item = new JdItem();
            //商品spu
            item.setSpu(spuId);
            //商品sku
            item.setSku(skuId);
            //商品url地址
            item.setUrl("https://item.jd.com/" + skuId + ".html");
            //创建时间
            item.setCreated(new Date());
            //修改时间
            item.setUpdated(item.getCreated());
            //获取商品标题
            String itemHtml = this.httpUtils.getHtml(item.getUrl());
            String title = Jsoup.parse(itemHtml).select("div.sku-name").text();
            item.setTitle(title);
            //获取商品价格
            String priceUrl = "https://p.3.cn/prices/mgets?skuIds=J_" + skuId;
            String priceJson = this.httpUtils.getHtml(priceUrl);
            //解析json数据获取商品价格
            double price = MAPPER.readTree(priceJson).get(0).get("p").asDouble();
            item.setPrice(price);

            //获取图片地址
            String pic = "https:" + spuEle.select(".p-img > a > img").attr("src").replace("/n9/", "/n1/");
            System.out.println(pic);
            //下载图片
            String picName = this.httpUtils.getImage(pic);
            item.setPic(picName);

            //保存商品数据
            this.jdItemService.save(item);

        }
    }


    /**
     * htmlunit爬虫
     */
    @GetMapping("/init")
    public void htmlunit() throws Exception {

    }

    public static void main(String[] args) throws Exception {
        WebClient webClient = HtmlUintUtil.getWebClient();
        int pageNumber = 1;
        HtmlPage page = null;
        while (true) {
            page = webClient.getPage("https://you.ctrip.com/searchsite/travels/?query=%e9%87%8d%e5%ba%86&isAnswered=&isRecommended=&publishDate=&PageNo=" + pageNumber);
            if (page == null) {
                break;
            }
            if (pageNumber == 2) {
                break;
            }
            List<HtmlElement> list = page.getByXPath("/html/body/div[2]/div[2]/div[2]/div/div[1]/ul/li");
            for (HtmlElement h:list){
//                HtmlElement a1 = h.getElementsByTagName("a").get(0);
//                HtmlElement a2 = h.getElementsByTagName("a").get(1);
//                System.out.println("title:"+a2.asText());
//                System.out.println("image:"+a1.getFirstElementChild().getAttribute("src"));
//                System.out.println("url:https://you.ctrip.com"+a1.getAttribute("href"));
                  HtmlPage detailPage = webClient.getPage("https://you.ctrip.com" + h.getElementsByTagName("a").get(0).getAttribute("href"));
                  HtmlElement firstByXPath = detailPage.getFirstByXPath("/html/body/div[2]/div[4]/div[1]/div[1]/div[2]");
                System.out.println("--------------------------------------------------------------------------------------------------------------------------");
                System.out.println("--------------------------------------------------------------------------------------------------------------------------");
                System.out.println("--------------------------------------------------------------------------------------------------------------------------");
                System.out.println("--------------------------------------------------------------------------------------------------------------------------");
                System.out.println("--------------------------------------------------------------------------------------------------------------------------");
                System.out.println("--------------------------------------------------------------------------------------------------------------------------");
                System.out.println("--------------------------------------------------------------------------------------------------------------------------");
                System.out.println("--------------------------------------------------------------------------------------------------------------------------");
                System.out.println(firstByXPath.asText());
            }
            pageNumber++;
        }


    }


}
