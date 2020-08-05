package com.bin.meishikecan.ThailandSite;

import com.bin.meishikecan.ThailandSite.taiguo.CNChillpainaiCrawsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TranslateServiceTest {

    @Autowired
    CNChillpainaiCrawsService cnChillpainaiCrawsService;

    @Test
    public void translateChillpainaiCrawsService() throws Exception{
        cnChillpainaiCrawsService.translate();
    }
}
