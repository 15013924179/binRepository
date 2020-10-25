package com.bin.meishikecan.common;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadVedioTest {

    /**
     * 通过视频的URL下载该视频并存入本地
     *
     * @param url      视频的URL
     * @param path 视频存入的文件
     */
    public static void download(String url,String path) throws Exception {
        File destFile = new File(path);

        destFile.createNewFile();

        URL videoUrl = new URL(url);

        URLConnection urlConnection = videoUrl.openConnection();

        urlConnection.setRequestProperty("user-agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");

        InputStream is = urlConnection.getInputStream();
        FileOutputStream fos = new FileOutputStream(destFile);

        int len = 0;
        byte[] buffer = new byte[1024];
        while ((-1) != (len = is.read(buffer))) {
            fos.write(buffer, 0, len);
        }
        fos.flush();

        if (null != fos) {
            fos.close();
        }

        if (null != is) {
            is.close();
        }
    }

    /**
     * 链接url 返回字节流
     *
     * @param url
     * @return
     * @throws IOException
     * @throws ProtocolException
     * @throws UnsupportedEncodingException
     */
    public static BufferedReader connectURL(URL url)
            throws IOException, ProtocolException, UnsupportedEncodingException {
        // 这里的代理服务器端口号 需要自己配置
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7959));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // 若遇到反爬机制则使用该方法将程序伪装为浏览器进行访问
        conn.setRequestMethod("GET");
        conn.setRequestProperty("user-agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        return br;
    }
}
