package com.itheima.java_fight.controller;


import com.itheima.java_fight.common.Result;
import com.itheima.java_fight.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Autowired
    public HelloService helloService;

    @GetMapping("/hello")
    public Result<String> sayHello(
            @RequestParam(name = "name",defaultValue = "guest")String name
                           ){
        String result = helloService.sayHello(name);
        return Result.success(result);

    }

}
