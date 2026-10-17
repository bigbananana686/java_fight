package com.itheima.java_fight.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ChatAnswer(
        String answer,
        @JsonPropertyDescription("本轮回答引用到的资料块编号，只填纯数字字符串，例如 \"154\"。不要带「块」字，不要带方括号。没有引用时给空数组。")
        List<String> citedIds) {
}