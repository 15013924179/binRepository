package com.bin.meishikecan.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@ToString
@Document(collection = "travel_document_mongodb")
@Data
public class TravelDocumentMongodb {
    @Id
    private String id;

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
