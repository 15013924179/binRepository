package com.bin.meishikecan.utils;

import org.openqa.selenium.WebDriver;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadVedioBySelenium {


    /**
     * @param m3u8 m3u8地址
     * @param path 保存地址
     */
    public static void start(String m3u8, String path,String name) throws Exception {

        //1.生产随机uuid
        System.out.println("生成随机目录");
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        String uuidStr = str.replace("-", "");

        File dir = new File(path + "\\" + uuidStr);
        if (!dir.exists()) {// 判断目录是否存在
            dir.mkdir();
        }

        //2.配置下载路径
        System.out.println("配置下载目录");
        WebDriver webDriver = MySeleniumUtils.getAddressDriver(path + "\\" + uuidStr);

        //3.下载m3u8
        System.out.println("下载m3u8文件");
        webDriver.get(m3u8);
        Thread.sleep(3000);

        //4.拿到m3u8并读取
        System.out.println("读取m3u8文件");
        String result = "";
        File file = dir.listFiles()[0];
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append(tempStr);
                sbf.append("\n");
            }
            reader.close();
            result = sbf.toString();
            //记得删掉m3u8不然会影响合并
            file.delete();
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

        //5.解析m3u8内容
        System.out.println("解析m3u8文件");
        List<String> datas = new ArrayList<>();
        String regex = "[0-9a-zA-Z.-]+.ts"; //正则表达式
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(result);
        while (m.find()) {
            datas.add(m.group());
        }

        //6.下载ts
        System.out.println("下载ts文件一共:" + datas.size() + "个");
        String uri = m3u8.replaceAll("\\w+.m3u8", "");
        int index = 1;
        for (String s : datas) {
            webDriver.get(uri + s);
            BigDecimal finish = new BigDecimal(index).divide(new BigDecimal(datas.size()), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
            System.out.println("完成度：" + finish + "%  (" + index + "/" + datas.size() + ")");
            index++;
        }

        //7.合并ts
        System.out.println("开始合并ts文件");

        String fileName = (name == null ? (path + "\\" + uuidStr + ".mp4") : (path + "\\" +name+".mp4"));
        Vector data = new Vector<>();
        Thread.sleep(5000);
        File[] files = sortByDate(path + "\\" + uuidStr);
        for (int i = 0; i < files.length; i++) {
            if (files[i].exists()) {
                data.add(new FileInputStream(files[i]));
            }
        }
        Enumeration<InputStream> en = data.elements();
        SequenceInputStream sis = new SequenceInputStream(en);
        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(fileName));
        byte[] bys = new byte[1024];
        int len = 0;
        while ((len = sis.read(bys)) != -1) {
            bos.write(bys, 0, len);
        }
        bos.close();
        sis.close();

        //8.ts文件
        System.out.println("删除ts文件");
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }

        //9.解密
        System.out.println("开始判断是否需要解密");
        String key = "";
        regex = "#EXT-X-KEY:.+"; //正则表达式
        pattern = Pattern.compile(regex);
        m = pattern.matcher(result);

        while (m.find()) {
            key = m.group();
        }

        if (StringUtil.isEmpty(key)) {
            System.out.println("该文件未加密或者找不到加密");
        } else {
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

            System.out.println("密钥下载地址:"+uri+key);

            Thread.sleep(5000);
            webDriver.get(uri + key);

            Thread.sleep(2000);
            file = null;
            for (File f : dir.listFiles()) {
                if (f.getName().equals(key)) {
                    file = f;
                }
            }
            if (file == null) {
                System.out.println("找不到key文件，无法解密！");
                return;
            }
            InputStream is = new FileInputStream(file);
            len = -1;//初始值，起标志位作用
            byte buf[] = new byte[1024];//缓冲区
            ByteArrayOutputStream baos = new ByteArrayOutputStream();//捕获内存缓冲区的数据转换为字节数组
            while ((len = is.read(buf)) != -1) {//循环读取内容,将输入流的内容放进缓冲区中
                baos.write(buf, 0, len);//将缓冲区内容写进输出流，0是从起始偏移量，len是指定的字符个数
            }
            key = new String(baos.toByteArray());
            file.delete();

            //开始解密
            System.out.println("开始解密,密钥为:" + key);
            File defile = new File(fileName);
            InputStream inputStream = new FileInputStream(defile);
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
            int date = -1;
            while ((date = bis.read()) != -1) {
                bos1.write(date);
            }
            byte[] bytes = bos1.toByteArray();

            byte[] decrypt = AESUtil.decrypt(bytes, key);
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(decrypt);

            fos.close();
            bos1.close();
            bis.close();
        }

        System.out.println("删除临时文件夹和文件");
        File[] files1 = dir.listFiles();
        for (int i=0;i<files1.length;i++){
            files1[i].delete();
        }
        dir.delete();
        System.out.println("执行完成");
        webDriver.quit();


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

}
