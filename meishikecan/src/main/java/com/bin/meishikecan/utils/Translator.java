package com.bin.meishikecan.utils;

import net.sf.json.JSONArray;

import java.net.*;
import java.io.*;

public class Translator {
    public String translate(String langFrom, String langTo,
                            String word) throws Exception {

        String url = "https://translate.googleapis.com/translate_a/single?" +
                "client=gtx&" +
                "sl=" + langFrom +
                "&tl=" + langTo +
                "&dt=t&q=" + URLEncoder.encode(word, "UTF-8");

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return parseResult(response.toString());
    }

    private String parseResult(String inputJson) throws Exception {
        JSONArray jsonArray = JSONArray.fromObject(inputJson);
        JSONArray jsonArray2 = (JSONArray) jsonArray.get(0);
        String result = "";

        for (int i = 0; i < jsonArray2.size(); i++) {
            result += ((JSONArray) jsonArray2.get(i)).get(0).toString();
        }

        return result;
    }


}