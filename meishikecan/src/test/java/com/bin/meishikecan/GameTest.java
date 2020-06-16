package com.bin.meishikecan;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

//引入注解@RunWith(Parameterized.class)参数化测试类
@RunWith(Parameterized.class)
public class GameTest {
    //引入待测试类，定义参数
    private Game game=new Game();

    private int num1;

    private int num2;

    private String expected;

    //编写需用到的参数的构造函数
    public GameTest(int num1, int num2,String expected) {
        super();
        this.num1 = num1;
        this.num2 = num2;
        this.expected=expected;
    }

    //编写参数数据初始化方法
    @Parameters
    public static Collection<Object[]> data(){
        return Arrays.asList(new Object[][] {{2,1,"夺奖成功!"},{2,2,"安慰奖!"},{1,2,"夺奖失败!"}});
    }

    @Test
    public void testWinGame() {
        String txt=game.winGame(num1, num2);
        Assert.assertEquals(expected, txt);
    }

}