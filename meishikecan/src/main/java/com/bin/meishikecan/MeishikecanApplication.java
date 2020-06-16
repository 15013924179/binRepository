package com.bin.meishikecan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("**.dao")
public class MeishikecanApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeishikecanApplication.class, args);
    }

}
