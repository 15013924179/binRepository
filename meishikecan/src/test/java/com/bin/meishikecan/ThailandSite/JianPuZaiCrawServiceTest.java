package com.bin.meishikecan.ThailandSite;

import com.bin.meishikecan.ThailandSite.jianpuzai.JianPuZaiBookingCrawsService;
import com.bin.meishikecan.ThailandSite.taiguo.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class JianPuZaiCrawServiceTest {

    @Resource
    JianPuZaiBookingCrawsService jianPuZaiBookingCrawsService;

    @Test
    public void bookingListPage() throws Exception{
        jianPuZaiBookingCrawsService.crawsListPage();
    }


}