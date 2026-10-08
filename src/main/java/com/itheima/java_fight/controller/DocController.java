package com.itheima.java_fight.controller;


import com.itheima.java_fight.common.Result;
import com.itheima.java_fight.service.DocService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class DocController {

    private final DocService docService;

    public DocController(DocService docService){
        this.docService = docService;
    }

    @PostMapping("/doc/upload")
    public Result<Integer>upload(@RequestParam("file")MultipartFile file){

        return Result.success(docService.upload(file));

    }

}
