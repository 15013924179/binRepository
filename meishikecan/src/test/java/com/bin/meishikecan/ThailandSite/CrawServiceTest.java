package com.bin.meishikecan.ThailandSite;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CrawServiceTest {

    @Autowired
    private ThailandNationalTourismCrawService thailandNationalTourismCrawService;

    @Autowired
    private TrueidCrawService travelTrueidCrawService;

    @Autowired
    private RyoiireviewCrawService ryoiireviewCrawService;

    @Autowired
    private ChillpainaiCrawsService chillpainaiCrawsService;

    @Autowired
    private WongnaiCrawService wongnaiCrawService;

    @Autowired
    private TripadvisorCrawsService tripadvisorCrawsService;

    @Autowired
    private WonderfulpackageCrawService wonderfulpackageCrawService;


    //https://thai.tourismthailand.org/
    @Test
    void thailandNationalTourismCrawListPage() throws Exception{
         thailandNationalTourismCrawService.crawDetailPage();
    }

    //https://travel.trueid.net/
    @Test
    void travelTrueidCrawListPage() throws Exception{
         travelTrueidCrawService.crawListPage();
    }

    //https://travel.trueid.net/
    @Test
    void travelTrueidCrawDetailPage1() throws Exception{
        travelTrueidCrawService.TypeDetail("trueid_travel");
    }

    //https://travel.trueid.net/
    @Test
    void travelTrueidCrawDetailPage2() throws Exception{
        travelTrueidCrawService.TypeDetail("trueid_hotel");
    }

    //https://travel.trueid.net/
    @Test
    void travelTrueidCrawDetailPage3() throws Exception{
        travelTrueidCrawService.TypeDetail("trueid_restaurant");
    }

    //https://www.ryoiireview.com
    @Test
    void RyoiireviewCrawListPage() throws Exception{
        ryoiireviewCrawService.crawListPage();
    }

    //https://www.ryoiireview.com
    @Test
    void RyoiireviewCrawDetailPage() throws Exception{
        ryoiireviewCrawService.crawDetailPage();
    }

    //https://www.chillpainai.com
    @Test
    void ChillpainaiCrawListPage() throws Exception{
        chillpainaiCrawsService.crawListPage();
    }

    //https://www.chillpainai.com
    @Test
    void ChillpainaiCrawDetailPage() throws Exception{
        chillpainaiCrawsService.crawDetailPage();
    }

    //https://www.wongnai.com/
    @Test
    void wongnaiCrawListPage() throws Exception{
        wongnaiCrawService.crawListPage("https://www.wongnai.com/businesses?domain=1","wongnai_restaurant",6,30);
    }

    //https://www.wongnai.com/
    @Test
    void wongnaiCrawListPage1() throws Exception{
        wongnaiCrawService.crawListPage("https://www.wongnai.com/businesses?domain=3","wongnai_hotel",1,71);
    }

    //https://www.wongnai.com/
    @Test
    void wongnaiCrawListPage2() throws Exception{
        wongnaiCrawService.crawTravelListPage("https://www.wongnai.com/businesses?domain=4","wongnai_travel",null,null);
    }

    //https://th.tripadvisor.com
    @Test
    void tripadvisorCrawListPage() throws Exception{
        tripadvisorCrawsService.crawListPage();
    }

    //https://th.tripadvisor.com
    @Test
    void tripadvisorCrawDetailPageHotel() throws Exception{
        tripadvisorCrawsService.crawDetailPageHotel();
    }

    //https://th.tripadvisor.com
    @Test
    void tripadvisorCrawDetailPageTravel() throws Exception{
        tripadvisorCrawsService.crawDetailPageTravel();
    }

    //https://th.tripadvisor.com
    @Test
    void tripadvisorCrawDetailPageRestan() throws Exception{
        tripadvisorCrawsService.crawDetailPageRestan();
    }

    //https://www.wonderfulpackage.com/product/Thailand/
    @Test
    void wonderfulpackageCrawListPage() throws Exception{
        wonderfulpackageCrawService.crawDetailPage();
    }

}