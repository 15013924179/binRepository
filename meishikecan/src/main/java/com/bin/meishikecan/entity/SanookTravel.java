package com.bin.meishikecan.entity;

import lombok.Data;

@Data
public class SanookTravel {
    private String id;

    private String title;

    private String url;

    private String content;

    private Long commentNumber;

    private String img;

    private String dataTime;

    private String siteType;

    private String crawlStatus;
}
