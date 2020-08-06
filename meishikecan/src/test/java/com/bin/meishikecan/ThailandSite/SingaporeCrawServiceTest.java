package com.bin.meishikecan.ThailandSite;

import com.bin.meishikecan.ThailandSite.xinjiapo.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SingaporeCrawServiceTest {

    @Autowired
    private SingaporeTripadvisorCrawsService singaporeTripadvisorCrawsService;

    @Autowired
    private SingaporeTrustCrawsService singaporeTrustCrawsService;

    @Autowired
    private BookingCrawsService bookingCrawsService;

    @Autowired
    private CtripCrawsService ctripCrawsService;

    @Autowired
    private MaFengWoCrawsService maFengWoCrawsService;

    @Test
    void singaporeTripadvisorCrawListPageHotel() throws Exception{
        singaporeTripadvisorCrawsService.crawListPageHotel();
    }

    @Test
    void singaporeTripadvisorCrawListPageRes() throws Exception{
        singaporeTripadvisorCrawsService.crawListPageRes();
    }

    @Test
    void singaporeTripadvisorCrawListPageTravel() throws Exception{
        singaporeTripadvisorCrawsService.crawListPageTravel();
    }

    @Test
    void singaporeTripadvisorCrawDetailPageHotel() throws Exception{
        singaporeTripadvisorCrawsService.crawDetailPageHotel();
    }

    @Test
    void singaporeTripadvisorCrawDetailPageTravel() throws Exception{
        singaporeTripadvisorCrawsService.crawDetailPageTravel();
    }

    @Test
    void singaporeTripadvisorCrawDetailPageRes() throws Exception{
        singaporeTripadvisorCrawsService.crawDetailPageRestan();
    }

    @Test
    void singaporeTrustCrawsListPage1() throws Exception{
        singaporeTrustCrawsService.crawListPage1();
    }

    @Test
    void singaporeTrustCrawsListPage2() throws Exception{
        singaporeTrustCrawsService.crawListPage2();
    }

    @Test
    void singaporeBookingCrawsListPage() throws Exception{
        bookingCrawsService.crawsListPage();
    }

    @Test
    void singaporeBookingCrawsDetailPage() throws Exception{
        bookingCrawsService.crawDetailPage();
    }

    @Test
    void singaporeMaFengWoCrawsDetailPage() throws Exception{
        maFengWoCrawsService.crawDetailPageByHotel();
    }

    @Test
    void singaporeCtripCrawsDetailPage() throws Exception{
        ctripCrawsService.crawDetailPageByTravel();
    }

    @Test
    void singaporeCtripCrawsListPage() throws Exception{
        ctripCrawsService.crawsListPageByRes();
    }
}
