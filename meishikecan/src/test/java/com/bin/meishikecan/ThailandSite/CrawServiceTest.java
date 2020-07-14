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

    @Autowired
    private SkyscannerCrawService skyscannerCrawService;

    @Autowired
    private SanookCrawService sanookCrawService;

    @Autowired
    private KapookCrawService kapookCrawService;

    @Autowired
    PaiduaykanCrawService paiduaykanCrawService;


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
        wongnaiCrawService.crawListPage("https://www.wongnai.com/businesses?domain=1","wongnai_restaurant",null,null);
    }

    //https://www.wongnai.com/
    @Test
    void wongnaiCrawListPage1() throws Exception{
        wongnaiCrawService.crawListPage("https://www.wongnai.com/businesses?domain=3","wongnai_hotel",null,null);
    }

    //https://www.wongnai.com/
    @Test
    void wongnaiCrawListPage2() throws Exception{
        wongnaiCrawService.crawTravelListPage("https://www.wongnai.com/businesses?domain=4","wongnai_travel",null,null);
    }

    //https://www.wongnai.com/
    @Test
    void wongnaiCrawDetailPage1() throws Exception{
        wongnaiCrawService.crawDetailPageTravel();
    }

    //https://www.wongnai.com/
    @Test
    void wongnaiCrawDetailPage2() throws Exception{
        wongnaiCrawService.crawDetailPageRes();
    }

    //https://www.wongnai.com/
    @Test
    void wongnaiCrawDetailPage3() throws Exception{
        wongnaiCrawService.crawDetailPageHotel();
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

    @Test
    void skyscannerCrawServiceCrawListPage() throws Exception{
        skyscannerCrawService.crawListPage();
    }

    //https://www.sanook.com/travel/thailand/
    @Test
    void sanookCrawListPageTravel() throws Exception{
        sanookCrawService.reptileListPage("https://www.sanook.com/travel/thailand/","sanook_travel");
    }

    //https://www.sanook.com/travel/restaurant/
    @Test
    void sanookCrawListPageRestaurant() throws Exception{
        sanookCrawService.reptileListPage("https://www.sanook.com/travel/restaurant/","sanook_restaurant");
    }

    //https://www.sanook.com/travel/hotel/
    @Test
    void sanookCrawListPageHotel() throws Exception{
        sanookCrawService.reptileListPage("https://www.sanook.com/travel/hotel/","sanook_hotel");
    }

    //https://www.sanook.com/travel/thailand/
    @Test
    void sanookCrawDetailPageTravel() throws Exception{
        sanookCrawService.reptileDetailPage("sanook_travel");
    }

    //https://www.sanook.com/travel/restaurant/
    @Test
    void sanookCrawDetailPageRestaurant() throws Exception{
        sanookCrawService.reptileDetailPage("sanook_restaurant");
    }

    //https://www.sanook.com/travel/hotel/
    @Test
    void sanookCrawDetailPageHotel() throws Exception{
        sanookCrawService.reptileDetailPage("sanook_hotel");
    }

    //https://travel.kapook.com/thai.html
    @Test
    void kapookCrawListPageTravel() throws Exception{
        kapookCrawService.reptileListPage("https://travel.kapook.com/thai.html","kapook_travel");
    }

    //https://travel.kapook.com/restaurant.html
    @Test
    void kapookCrawListPageRestaurant() throws Exception{
        kapookCrawService.reptileListPage("https://travel.kapook.com/restaurant.html","kapook_restaurant");
    }

    //https://travel.kapook.com/hotel.html
    @Test
    void kapookCrawListPageHotel() throws Exception{
        kapookCrawService.reptileListPage("https://travel.kapook.com/hotel.html","kapook_hotel");
    }

    //https://travel.kapook.com/hotel.html
    @Test
    void kapookCrawDetailPageHotel() throws Exception{
        kapookCrawService.reptileDetailPage("kapook_hotel");
    }

    //https://travel.kapook.com/thai.html
    @Test
    void kapookCrawDetailPageTravel() throws Exception{
        kapookCrawService.reptileDetailPage("kapook_travel");
    }

    //https://travel.kapook.com/restaurant.html
    @Test
    void kapookCrawDetailPageRestaurant() throws Exception{
        kapookCrawService.reptileDetailPage("kapook_restaurant");
    }

    //https://www.paiduaykan.com
    @Test
    void paiduaykanCrawListPage() throws Exception{
        paiduaykanCrawService.crawDetailPage();
    }

    //https://www.skyscanner.co.th
    @Test
    void skyscannerCrawListPage() throws Exception{
        skyscannerCrawService.crawDetailPage();
    }



}