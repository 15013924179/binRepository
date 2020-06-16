package com.bin.meishikecan.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class Test {

    public Test(){
        System.out.println("i am Test");
    }

    public static void main(String[] args) {
        Test t=new Test();
    }


}

class TestChildren extends Test{


    public TestChildren(){
        System.out.println("i am TestChildren");
    }

    public static void main(String[] args) {

    }

}
