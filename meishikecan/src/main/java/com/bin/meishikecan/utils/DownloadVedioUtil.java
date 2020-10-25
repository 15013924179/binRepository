package com.bin.meishikecan.utils;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.proxy.CaptureType;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 下载视频工具类
 */
public class DownloadVedioUtil {

    /**
     * @param m3u8 m3u8地址
     * @param type 1 本地 2 url
     * @param path 保存地址
     */
    public static void start(String m3u8, Integer type, String path, String preUrl) throws Exception {

        //下载
        List<String> result = download(type, m3u8, preUrl, path);
        //合并
        merge(result);

        System.out.println("执行完成");
    }

    public static List<String> download(Integer type, String m3u8, String preUrl, String path) throws Exception {

        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        HttpURLConnection httpUrl = null;
        URL url = null;
        String result = "";
        String uri = "";

        if (type == 1) {

            //读取m3u8文件
            File file = new File(m3u8);
            BufferedReader reader = null;
            StringBuffer sbf = new StringBuffer();
            try {
                reader = new BufferedReader(new FileReader(file));
                String tempStr;
                while ((tempStr = reader.readLine()) != null) {
                    sbf.append(tempStr);
                }
                reader.close();
                result = sbf.toString();
                uri = preUrl;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }

        }
        if (type == 2) {

            url = new URL(m3u8);
//            // 创建代理服务器
            httpUrl = (HttpURLConnection) url.openConnection();
            httpUrl.setRequestProperty("User-Agent", "Mozilla/31.0 (compatible; MSIE 10.0; Windows NT; DigExt)");
            httpUrl.setConnectTimeout(10*1000);
            httpUrl.setReadTimeout(10*1000);
            httpUrl.connect();
            //拿到服务器返回的InputStream
            InputStream is = httpUrl.getInputStream();
            //将从服务器获得的流is转换为字符串
            int len = -1;//初始值，起标志位作用
            byte buf[] = new byte[1024];//缓冲区
            ByteArrayOutputStream baos = new ByteArrayOutputStream();//捕获内存缓冲区的数据转换为字节数组
            while ((len = is.read(buf)) != -1) {//循环读取内容,将输入流的内容放进缓冲区中
                baos.write(buf, 0, len);//将缓冲区内容写进输出流，0是从起始偏移量，len是指定的字符个数
            }
            result = new String(baos.toByteArray());//最终结果，将字节数组转换

            //截取url前缀
            uri = m3u8.replaceAll("\\w+.m3u8", "");

        }

            //正则匹配筛选
            String regex = "[0-9a-zA-Z.-]+.ts"; //正则表达式
            Pattern pattern = Pattern.compile(regex);
            Matcher m = pattern.matcher(result);
            List<String> datas = new ArrayList<>();

            while (m.find()) {
                datas.add(m.group());
            }

        System.out.println("ts个数:" + datas.size());

        int index = 1;

        //生产随机uuid
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        String uuidStr = str.replace("-", "");

        File dir = new File(path + "\\" + uuidStr);
        if (!dir.exists()) {// 判断目录是否存在
            dir.mkdir();
        }

        //开始下载
        try {
            System.out.println("开始下载");
            for (String s : datas) {

                byte[] buf1 = new byte[1024];
                int size = 0;

                url = new URL(uri + s);
                httpUrl = (HttpURLConnection) url.openConnection();
                httpUrl.setRequestProperty("User-Agent", "Mozilla/31.0 (compatible; MSIE 10.0; Windows NT; DigExt)");
                httpUrl.connect();
                bis = new BufferedInputStream(httpUrl.getInputStream());
                fos = new FileOutputStream(path + "\\" + uuidStr + "\\" + index + ".ts");
                while ((size = bis.read(buf1)) != -1)
                    fos.write(buf1, 0, size);
                fos.close();
                bis.close();
                httpUrl.disconnect();

                BigDecimal finish = new BigDecimal(index).divide(new BigDecimal(datas.size()), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
                System.out.println("完成度：" + finish + "%");

                index++;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("下载完成");

        List<String> list = new ArrayList<>();
        //m3u8内容
        list.add(result);
        //下载地址
        list.add(path + "\\" + uuidStr);
        //url前缀
        list.add(uri);
        return list;
    }

    //合并
    public static void merge(List<String> list) throws Exception {

        String path = list.get(1);

        String m3u8 = list.get(0);

        String uri = list.get(2);

        System.out.println("加载完毕，开始合并数据");

        Vector data = new Vector<>();

        File[] files = sortByDate(path);

        for (int i = 0; i < files.length; i++) {
            data.add(new FileInputStream(files[i]));
        }

        Enumeration<InputStream> en = data.elements();
        SequenceInputStream sis = new SequenceInputStream(en);
        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(path + "\\video.mp4"));
        // 如何写读写呢，其实很简单，你就按照以前怎么读写，现在还是怎么读写
        byte[] bys = new byte[1024];
        int len = 0;
        while ((len = sis.read(bys)) != -1) {
            bos.write(bys, 0, len);
        }

        bos.close();
        sis.close();

        System.out.println("删除ts文件");
        //删除其他ts文件
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }

        //判断是否需要解密
        String key = "";
        String regex = "#EXT-X-KEY:.+"; //正则表达式
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(m3u8);

        while (m.find()) {
            key = m.group();
        }

        if (StringUtil.isEmpty(key)) {
            System.out.println("该文件未加密或者找不到加密");
        }

        System.out.println("密钥：" + key);

        String[] split = key.replace("#EXT-X-KEY:", "").split(",");

        if (split.length != 2) {
            System.out.println("密钥格式不正确");
        }

        if (!split[0].split("=")[1].contains("AES")) {
            System.out.println("未知算法,退出");
            return;
        }

        key = split[1].split("=")[1].replace("\"", "");


        URL url = new URL(uri+key);
        HttpURLConnection httpUrl = (HttpURLConnection) url.openConnection();
        httpUrl.setRequestProperty("User-Agent", "Mozilla/31.0 (compatible; MSIE 10.0; Windows NT; DigExt)");
        httpUrl.connect();
        //拿到服务器返回的InputStream
        InputStream is = httpUrl.getInputStream();
        //将从服务器获得的流is转换为字符串
        len = -1;//初始值，起标志位作用
        byte buf[] = new byte[1024];//缓冲区
        ByteArrayOutputStream baos = new ByteArrayOutputStream();//捕获内存缓冲区的数据转换为字节数组
        while ((len = is.read(buf)) != -1) {//循环读取内容,将输入流的内容放进缓冲区中
            baos.write(buf, 0, len);//将缓冲区内容写进输出流，0是从起始偏移量，len是指定的字符个数
        }
        //得到key
        key = new String(baos.toByteArray());

        //开始解密
        System.out.println("开始解密");
        File file = new File(path + "\\video.mp4");
        InputStream inputStream = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        int date = -1;
        while ((date = bis.read()) != -1) {
            bos1.write(date);
        }
        byte[] bytes = bos1.toByteArray();

        byte[] decrypt = AESUtil.decrypt(bytes, key);
        FileOutputStream fos = new FileOutputStream(path + "\\video.mp4");
        fos.write(decrypt);
        fos.close();
        bos1.close();
        bis.close();
    }

    /**
     * 排序文件
     *
     * @param filePath
     * @return
     */
    public static File[] sortByDate(String filePath) {
        File file = new File(filePath);
        File[] files = file.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff > 0)
                    return 1;
                else if (diff == 0)
                    return 0;
                else
                    return -1;//如果 if 中修改为 返回-1 同时此处修改为返回 1  排序就会是递减
            }

            public boolean equals(Object obj) {
                return true;
            }

        });
        return files;

    }

    public static void main(String[] args) throws Exception {

    }

}
