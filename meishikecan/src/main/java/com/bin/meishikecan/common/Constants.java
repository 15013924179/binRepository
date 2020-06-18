package com.bin.meishikecan.common;

import lombok.Getter;
import net.sf.json.JSONObject;

@Getter
public enum  Constants {

    OK(20000, "OK"),
    isNotNull(40000,"参数不能为空"),
    SystemError(50000,"系统错误");

    private final int code;

    private final String message;

    Constants(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String toJson(Object obj) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code",code);
        jsonObject.put("message",message);
        jsonObject.put("data",obj==null?"":obj);
        return jsonObject.toString();
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code",code);
        jsonObject.put("message",message);
        return jsonObject.toString();
    }

    public String toErrorJson(String message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code",code);
        jsonObject.put("message",message);
        return jsonObject.toString();
    }

}
