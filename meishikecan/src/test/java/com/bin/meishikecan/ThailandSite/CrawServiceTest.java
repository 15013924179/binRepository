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
        wongnaiCrawService.crawListPage("https://www.wongnai.com/businesses?domain=1","wongnai_restaurant",4,1,"//*[@class=\"sc-10ino0a-13 dKFLtJ\"]");
    }

    //https://www.wongnai.com/
    @Test
    void wongnaiCrawListPage1() throws Exception{
        wongnaiCrawService.crawListPage("https://www.wongnai.com/businesses?domain=3","wongnai_hotel",null,null,"//*[@class=\"sc-10ino0a-13 dKFLtJ\"]");
    }

    //https://www.wongnai.com/
    @Test
    void wongnaiCrawListPage2() throws Exception{
        wongnaiCrawService.crawListPage("https://www.wongnai.com/businesses?domain=4","wongnai_travel",null,null,"//*[@class=\"sc-7z7jpy-3 hYHNzB\"]");
    }

    //https://th.tripadvisor.com
    @Test
    void tripadvisorCrawListPage() throws Exception{
        tripadvisorCrawsService.crawListPage();
    }


}