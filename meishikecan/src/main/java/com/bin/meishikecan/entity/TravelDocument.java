package com.bin.meishikecan.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Table
@Entity
public class TravelDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String url;

    private String likeNumber;

    private String commentNumber;

    private String browseNumber;

    private String author;

    private String authorImage;

    private String topImage;

    private String createTime;

    private String updateTime;

    private String content;

}
