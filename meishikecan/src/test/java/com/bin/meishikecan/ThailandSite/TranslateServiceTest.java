package com.bin.meishikecan.ThailandSite;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class TranslateServiceTest {

    @Resource
    private TranslateService translateService;

    @Test
    public void ryoiireviewRes() throws Exception {

        List<String> list = new ArrayList();

        list.add("title");
        list.add("content");

        translateService.translate("ryoiireview_restaurant", list, "th", "zh");
    }

    @Test
    public void sanookHotel() throws Exception {

        List<String> list = new ArrayList();

        list.add("title");
        list.add("content");

        translateService.translate("sanook_hotel", list, "th", "zh");
    }

    @Test
    public void sanookRes() throws Exception {

        List<String> list = new ArrayList();

        list.add("title");
        list.add("content");

        translateService.translate("sanook_restaurant", list, "th", "zh");
    }

    @Test
    public void sanookTravel() throws Exception {

        List<String> list = new ArrayList();

        list.add("title");
        list.add("content");

        translateService.translate("sanook_travel", list, "th", "zh");
    }


}
