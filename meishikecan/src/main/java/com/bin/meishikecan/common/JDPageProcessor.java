package com.bin.meishikecan.common;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

public class JDPageProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(3000).setUserAgent(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {

        if (!page.getUrl().regex("https://item.jd.com/[0-9]+.html").match()) {

            //列表頁
            page.addTargetRequests(page.getHtml().xpath("//*[@id=\"J_goodsList\"]/ul/li/div[1]/div[1]/a/@href").all());

        } else {
            page.putField("title",page.getHtml().xpath("//*[@class=\"sku-name\"]/text()").toString());
            page.putField("price",page.getHtml().xpath("/html/body/div[6]/div/div[2]/div[3]/div/div[1]/div[2]/span[1]/span[2]/text()").toString());
            page.putField("score",page.getHtml().xpath("//*[@id=\"comment-count\"]/a/text()").toString());
            page.putField("content",page.getHtml().getDocument().getElementsByClass("p-parameter").text());
            page.putField("url",page.getUrl().toString());
        }

    }

    @Override
    public Site getSite() {
        return site;
    }


}
