package com.bin.meishikecan.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

public class ProxyHttpUtils {

    private static String url = "http://http.tiqu.alicdns.com/getip3?num=1&type=1&pro=&city=0&yys=0&port=1&pack=114021&ts=0&ys=0&cs=0&lb=1&sb=0&pb=4&mr=1&regions=&gm=4";

    static RestTemplate restTemplate = new RestTemplate();

    public static TranslateThreadLocal getipAndPort() {

        TranslateThreadLocal translateThreadLocal = new TranslateThreadLocal();

        String response = restTemplate.getForObject(url, String.class);

        String[] arr = response.trim().split(":");

        if (arr == null || arr.length != 2) {
            return null;
        }

        translateThreadLocal.setIp(arr[0]);
        translateThreadLocal.setPort(Integer.parseInt(arr[1]));
        return translateThreadLocal;

    }

    public static void main(String[] args) {
        System.out.println(ProxyHttpUtils.getipAndPort());
    }


}
