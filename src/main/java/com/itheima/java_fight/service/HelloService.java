package com.itheima.java_fight.service;

import org.springframework.stereotype.Service;

@Service
public class HelloService {

    public String sayHello(String name){
        String result = "hello "+name;
        return result;
    }

}
