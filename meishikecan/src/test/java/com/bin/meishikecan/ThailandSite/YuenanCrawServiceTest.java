package com.bin.meishikecan.ThailandSite;

import com.bin.meishikecan.ThailandSite.yuenan.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class YuenanCrawServiceTest {

    @Resource
    YuenanTripadvisorCrawsService yuenanTripadvisorCrawsService;

    @Resource
    YuenanVietnammmCrawService yuenanVietnammmCrawService;

    @Resource
    YuenanBookingCrawsService yuenanBookingCrawsService;

    @Resource
    YuenanMytourCrawService yuenanMytourCrawService;

    @Resource
    YuenanIvivuCrawService yuenanIvivuCrawService;

    @Test
    public void TripadvisorListPage() throws Exception{
        yuenanTripadvisorCrawsService.crawListPageRes();
    }

    @Test
    public void TripadvisorDetailPage() throws Exception{
        yuenanTripadvisorCrawsService.crawDetailPageHotel();
    }

    @Test
    public void VietnammmListPage1() throws Exception{
        yuenanVietnammmCrawService.crawDetailPage();
    }

    @Test
    public void BookingListPage() throws  Exception {
        yuenanBookingCrawsService.crawDetailPage();
    }


    @Test
    public void MytourListPage() throws Exception {
        yuenanMytourCrawService.crawsListPage();
    }

    @Test
    public void ivivuListPage() throws Exception {
        yuenanIvivuCrawService.crawListPage();
    }


}
