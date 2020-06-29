package com.bin.meishikecan.config;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import lombok.Data;

public class HtmlUintUtil {

    private static WebClient webClient;

    static {
        //新建一个WebClient 模拟谷歌Chrome版本67
        webClient = new WebClient(BrowserVersion.BEST_SUPPORTED.CHROME);
        // 关闭ActiveX
        webClient.getOptions().setActiveXNative(false);
        //CSS默认true
        webClient.getOptions().setCssEnabled(true);
        //js默认true
        webClient.getOptions().setJavaScriptEnabled(true);
        // 支持重定向
        webClient.getOptions().setRedirectEnabled(true);
        //很重要，设置支持AJAX
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        //关闭  JS执行出错 异常
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        //设置请求超时时间
        webClient.getOptions().setTimeout(15000);
        // 设置当返回失败状态码时不打印
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        //当HTTP的状态非200时是否抛出异常
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);

        webClient.waitForBackgroundJavaScript(3000);

    }

    private HtmlUintUtil(){

    }

    public static WebClient getWebClient(){
        return webClient;
    }

}
