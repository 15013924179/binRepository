package com.bin.meishikecan.ThailandSite;

import com.bin.meishikecan.reptile.SanookReptile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CrawTest {

    @Autowired
    private CrawTemplateService crawTemplateService;

    @Test
    public void jianpuzai_booking() throws Exception{
//        crawTemplateService.bookingListPage("https://www.booking.com/searchresults.en-gb.html?aid=1815331&label=kh-autFviVTnRQTouyT6JCpBwS381452403077%3Apl%3Ata%3Ap1%3Ap2%3Aac%3Aap%3Aneg%3Afi%3Atiaud-294889296213%3Akwd-16182756352%3Alp9066820%3Ali%3Adec%3Adm%3Appccp%3DUmFuZG9tSVYkc2RlIyh9YYzu_e_JczYp9KPuwt_Sn0E&lang=en-gb&sid=3e507b59d1aeefb4e72d9dc447e56916&sb=1&sb_lp=1&src=country&src_elem=sb&error_url=https%3A%2F%2Fwww.booking.com%2Fcountry%2Fkh.en-gb.html%3Faid%3D1815331%3Blabel%3Dkh-autFviVTnRQTouyT6JCpBwS381452403077%253Apl%253Ata%253Ap1%253Ap2%253Aac%253Aap%253Aneg%253Afi%253Atiaud-294889296213%253Akwd-16182756352%253Alp9066820%253Ali%253Adec%253Adm%253Appccp%253DUmFuZG9tSVYkc2RlIyh9YYzu_e_JczYp9KPuwt_Sn0E%3Bsid%3D3e507b59d1aeefb4e72d9dc447e56916%3B&ss=Cambodia&is_ski_area=0&checkin_year=2020&checkin_month=9&checkin_monthday=1&checkout_year=2020&checkout_month=9&checkout_monthday=2&group_adults=2&group_children=0&no_rooms=1&b_h4u_keep_filters=&from_sf=1&ss_raw=Cambodia&ac_position=0&ac_langcode=en&ac_click_type=b&dest_id=36&dest_type=country&place_id_lat=12.5657&place_id_lon=104.991&search_pageview_id=d1a01869fd7300fd&search_selected=true"
//        ,"jianpuzai_booking_hotel");
          crawTemplateService.bookingDetailPage("jianpuzai_booking_hotel");
    }

    @Test
    public void jianpuzai_tripadvisor_Hotel() throws Exception{
//        crawTemplateService.tripadvisorListPageByHotel("https://www.tripadvisor.com/Hotels-g293939-Cambodia-Hotels.html","jianpuzai_tripadvisor_hotel");
        crawTemplateService.tripadvisorDetailPageByHotel("jianpuzai_tripadvisor_hotel");
    }

    @Test
    public void jianpuzai_tripadvisor_Travel() throws Exception{
        crawTemplateService.tripadvisorListPageTravel("https://www.tripadvisor.com/Search?q=Cambodia&searchSessionId=AAA6C5022DAC179904470524CA4918C91598948510386ssid&searchNearby=false&geo=293939&sid=B7D33718E55ECC4647ADED54590F7E861598948514611&blockRedirect=true&ssrc=A&rf=2"
        ,"jianpuzai_tripadvisor_travel");
    }

    @Test
    public void jianpuzai_tripadvisor_Res() throws Exception{
        crawTemplateService.tripadvisorDetailPageRes("jianpuzai_tripadvisor_restaurant");
    }

}
