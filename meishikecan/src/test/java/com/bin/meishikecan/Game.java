package com.bin.meishikecan;

public class Game {

    public String winGame(int num1, int num2) {
        int num = num1 - num2;
        String txt = null;
        if (num > 0) {
            txt = "夺奖成功!";
        } else if (num == 0) {
            txt = "安慰奖!";
        } else {
            txt = "夺奖失败!";
        }
        return txt;
    }
}