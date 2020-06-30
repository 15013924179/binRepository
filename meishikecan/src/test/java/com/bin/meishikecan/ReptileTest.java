package com.bin.meishikecan;

import com.bin.meishikecan.reptile.SanookReptile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ReptileTest {

    @Autowired
    private SanookReptile sanookReptile;

    @Test
    void replite1() throws Exception{
        sanookReptile.reptileListPage();
    }

    @Test
    void replite2() throws Exception{
        sanookReptile.reptileDetailPage();
    }

}
