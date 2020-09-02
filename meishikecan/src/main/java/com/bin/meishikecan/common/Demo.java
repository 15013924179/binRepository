package com.bin.meishikecan.common;

import org.apache.commons.collections.Bag;
import org.apache.commons.collections.bag.HashBag;

import java.util.*;
import java.util.stream.Collectors;

public class Demo {

    public static boolean containsPattern(int[] arr, int m, int k) {

        int num = 1;

        int oldNum = 1;

        List<Integer> list = Arrays.stream(arr).boxed().collect(Collectors.toList());

        for (int i = 0 ;i< arr.length-m ; i++) {
            List<Integer> l = list.subList(i, i+m);
            for (int j = i+m ; j<=arr.length-m ; j=j+m) {
                if (equalList(l,list.subList(j,j+m))){
                    num++;
                }else{
                    break;
                }
            }
            if (oldNum < num) {
                oldNum = num;
            }
            num = 1;
        }

        if (k <= oldNum) {
            return true;
        }else{
            return false;
        }
    }


    public static boolean equalList(List<Integer> list1, List<Integer> list2){
        for (int i =0;i<list1.size();i++) {
            if (!list1.get(i).equals(list2.get(i))){
                return false;
            }
        }
        return true;
    }

    public static int getMaxLen(int[] nums) {
        int num = 0;
        for (int i= nums.length; i>0;i--) {
            for (int j = 0;j<=nums.length-i ;j++) {
                int[] newArray = Arrays.copyOfRange(nums, j, j+i);
                if (get(newArray)>0){
                    num=i;
                    break;
                }
            }
            if (num == i) {
                break;
            }
        }

        return num;

    }

    public static int get (int[] arr) {
        int a = 1;

        for (int i = 0;i<arr.length;i++) {

            int abs = 0;

            if(arr[i] !=0){
                abs = Math.abs(arr[i]);
            }

            a = a*(arr[i]/abs);
        }

        return a;
    }

    public static void main(String[] args) {

        int[] arr={1000000000,1000000000};

        int maxLen = getMaxLen(arr);

        System.out.println(maxLen);


    }

}
