package com.lemon.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PhoneRandom {
    /**
     * 随机生成一个手机号码
     */
    public static String getPhone(){
        List<String> list1 = new ArrayList<String>();
        //["13","15","17","18","19"];
        list1.add("134");
        list1.add("136");
        list1.add("137");
        list1.add("138");
        list1.add("139");
        list1.add("150");
        list1.add("151");
        list1.add("152");
        list1.add("157");
        list1.add("158");
        list1.add("159");
        Random random = new Random();
        int num1 = random.nextInt(list1.size());
        String phonePrefix = list1.get(num1);
        for (int i = 0;i < 8;i++) {
            int num2 = random.nextInt(9);
            phonePrefix += num2;
        }
        return phonePrefix;

    }

    /**
     * 获取一个数据库当中没有被注册过的手机号码
     */
    public static String getRandomPhone(){
        while(true) {
            String phone = getPhone();
            Object result = JDBCUtils.querySingle("select count(*) from member where MobilePhone=" + phone);
            if ((Long) result == 1){
                continue;
            }else {
                return phone;
            }
        }
    }

    public static void main(String[] args) {
        //解决每次注册运行的时候手机号码需要手动更改饿问题
        //1、先来随机生成手机号码（可能已经被注册过）
        //2、查询数据库，如果有注册的话，再来随机生成
        //3、1-2循环运行，知道产生一个没有注册过的手机号码即可
        String phone = getRandomPhone();
        System.out.println(phone);

    }
}
