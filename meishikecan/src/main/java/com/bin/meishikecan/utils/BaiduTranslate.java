package com.bin.meishikecan.utils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class BaiduTranslate {

    private final static String APPID = "20200717000521112";

    private final static String URL = "http://api.fanyi.baidu.com/api/trans/vip/translate";

    private final static String securityKey = "";

    private final static String APPID2 = "";

    private final static String securityKey2 = "";

    // 首先初始化一个字符数组，用来存放每个16进制字符
    private static final char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f'};

    public static String translateText(String content, String from, String to) throws Exception {

        String reponseString = null;

        CloseableHttpClient hc = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(URL);

        CloseableHttpResponse response;
        try {
            // 随机数
            String salt = String.valueOf(System.currentTimeMillis());
            //appid+q+salt+密钥
            // 签名
            String sign = APPID + content + salt + securityKey; // 加密前的原文


            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("q", content));
            params.add(new BasicNameValuePair("from", from));
            params.add(new BasicNameValuePair("to", to));
            params.add(new BasicNameValuePair("appid", APPID));
            params.add(new BasicNameValuePair("salt", salt));
            params.add(new BasicNameValuePair("sign", md5(sign)));


            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            response = hc.execute(httpPost);
            HttpEntity entity = response.getEntity();
            reponseString = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            reponseString = returnResult(reponseString);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpPost.releaseConnection();
        }

        return reponseString;
    }

    public static String translateText2(String content, String from, String to) throws Exception {

        String reponseString = null;

        CloseableHttpClient hc = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(URL);

        CloseableHttpResponse response;
        try {
            // 随机数
            String salt = String.valueOf(System.currentTimeMillis());
            //appid+q+salt+密钥
            // 签名
            String sign = APPID2 + content + salt + securityKey2; // 加密前的原文


            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("q", content));
            params.add(new BasicNameValuePair("from", from));
            params.add(new BasicNameValuePair("to", to));
            params.add(new BasicNameValuePair("appid", APPID2));
            params.add(new BasicNameValuePair("salt", salt));
            params.add(new BasicNameValuePair("sign", md5(sign)));


            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            response = hc.execute(httpPost);
            HttpEntity entity = response.getEntity();
            reponseString = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            reponseString = returnResult(reponseString);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpPost.releaseConnection();
        }

        return reponseString;
    }

    /**
     * 获得一个字符串的MD5值
     *
     * @param input 输入的字符串
     * @return 输入字符串的MD5值
     */
    public static String md5(String input) throws Exception {
        if (input == null)
            return null;

        try {
            // 拿到一个MD5转换器（如果想要SHA1参数换成”SHA1”）
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            // 输入的字符串转换成字节数组
            byte[] inputByteArray = input.getBytes("utf-8");
            // inputByteArray是输入字符串转换得到的字节数组
            messageDigest.update(inputByteArray);
            // 转换并返回结果，也是字节数组，包含16个元素
            byte[] resultByteArray = messageDigest.digest();
            // 字符数组转换成字符串返回
            return byteArrayToHex(resultByteArray);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private static String byteArrayToHex(byte[] byteArray) {
        // new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方））
        char[] resultCharArray = new char[byteArray.length * 2];
        // 遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去
        int index = 0;
        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }

        // 字符数组组合成字符串返回
        return new String(resultCharArray);

    }

    public static String returnResult(String response) {
        JSONObject jsonObject = JSONObject.fromObject(response);
        Object msg = jsonObject.get("trans_result");
        JSONArray array = JSONArray.fromObject(msg);
        String res = "";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < array.size(); i++) {
            JSONObject ob = (JSONObject) array.get(i);
            stringBuilder.append(ob.getString("dst")+"\n");
        }

        res=stringBuilder.toString();

        return decodeUnicode(res);
    }



    public static String decodeUnicode(String utfString) {
        StringBuilder sb = new StringBuilder();
        int i = -1;
        int pos = 0;

        while ((i = utfString.indexOf("\\u", pos)) != -1) {
            sb.append(utfString.substring(pos, i));
            if (i + 5 < utfString.length()) {
                pos = i + 6;
                sb.append((char) Integer.parseInt(utfString.substring(i + 2, i + 6), 16));
            }
        }
        sb.append(utfString.substring(pos));
        return sb.toString();
    }



    public static void main(String[] args) throws Exception {
        System.out.println(BaiduTranslate.translateText("สถานที่ขอพรให้ปังทั้งความรักการงาน สุขภาพ!", "th", "zh"));
    }
}
