package com.bin.meishikecan.controller;


import com.bin.meishikecan.common.BlogModel;
import com.bin.meishikecan.common.ReturnJson;
import com.bin.meishikecan.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/blog")
public class BlogController {
    @Autowired
    private BlogService blogService;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @PostMapping("/add")
    public String add(@RequestBody BlogModel blogModel) {
        blogService.save(blogModel);
        return ReturnJson.success();
    }

    @GetMapping("/get/{id}")
    public String getById(@PathVariable String id) {
        if (StringUtils.isEmpty(id))
            return ReturnJson.error();
        Optional<BlogModel> blogModelOptional = blogService.findById(id);
        if (blogModelOptional.isPresent()) {
            BlogModel blogModel = blogModelOptional.get();
            return ReturnJson.success(blogModel);
        }
        return ReturnJson.error();
    }

    @PostMapping("/update")
    public String updateById(@RequestBody BlogModel blogModel) {
        String id = blogModel.getId();
        if (StringUtils.isEmpty(id))
            return ReturnJson.error();;
        blogService.save(blogModel);
        return ReturnJson.success();
    }

    @DeleteMapping("/delete/{id}")
    public String deleteById(@PathVariable String id) {
        if (StringUtils.isEmpty(id))
            return ReturnJson.error();
        blogService.deleteById(id);
        return ReturnJson.success();
    }

    @GetMapping("/rep/search/title")
    public String repSearchTitle(String keyword) {
        if (StringUtils.isEmpty(keyword))
            return ReturnJson.error();
        List<BlogModel> byTitleLike = blogService.findByTitleLike(keyword);
        return ReturnJson.success(byTitleLike);
    }

    @GetMapping("/rep/search/title/custom")
    public String repSearchTitleCustom(String keyword) {
        if (StringUtils.isEmpty(keyword))
            return ReturnJson.error();
        return ReturnJson.success(blogService.findByTitleCustom(keyword));
    }
}
