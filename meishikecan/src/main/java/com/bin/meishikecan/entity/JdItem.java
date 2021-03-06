package com.bin.meishikecan.entity;

import lombok.Data;

import java.util.Date;

@Data
public class JdItem {
        //主键
        private Long id;
        //标准产品单位（商品集合）
        private Long spu;
        //库存量单位（最小品类单元）
        private Long sku;
        //商品标题
        private String title;
        //商品价格
        private Double price;
        //商品图片
        private String pic;
        //商品详情地址
        private String url;
        //创建时间
        private Date created;
        //更新时间
        private Date updated;

}
