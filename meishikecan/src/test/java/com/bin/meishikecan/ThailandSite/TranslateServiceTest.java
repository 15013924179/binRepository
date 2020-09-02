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

    @Test
    public void xinjiapo_tripadvisor_hotel() throws Exception {

        List<String> list = new ArrayList();

        list.add("title");
        list.add("address");
        list.add("rank");

        translateService.translate("xinjiapo_tripadvisor_hotel", list, "en", "zh");
    }

    @Test
    public void xinjiapo_tripadvisor_restaurant() throws Exception {

        List<String> list = new ArrayList();

        list.add("title");
        list.add("address");
        list.add("rank");

        translateService.translate("xinjiapo_tripadvisor_restaurant", list, "en", "zh");
    }

    @Test
    public void xinjiapo_tripadvisor_travel() throws Exception {

        List<String> list = new ArrayList();

        list.add("title");
        list.add("address");
        list.add("rank");

        translateService.translate("xinjiapo_tripadvisor_travel", list, "en", "zh");
    }

    @Test
    public void xinjiapo_trust_guide() throws Exception {

        List<String> list = new ArrayList();

        list.add("name");

        translateService.translate("xinjiapo_trust_guide", list, "en", "zh");
    }

    @Test
    public void xinjiapo_trust_travel_agency() throws Exception {

        List<String> list = new ArrayList();

        list.add("title");
        list.add("address");

        translateService.translate("xinjiapo_trust_travel_agency", list, "en", "zh");
    }









}
