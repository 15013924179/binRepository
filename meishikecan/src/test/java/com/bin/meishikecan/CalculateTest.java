package com.bin.meishikecan;

import org.junit.Assert;
import org.junit.Test;

/*
   简单方法单元测试
 */
public class CalculateTest {

    @Test
    public void testAdd(){
        Assert.assertEquals(3,new Calculate().add(1,2));
    }


    @Test
    public void testSubtract(){
        Assert.assertEquals(2,new Calculate().subtract(4,2));
    }
}
