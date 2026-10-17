package com.itheima.java_fight.dto;

import java.util.List;

public record ChatAnswer(String answer, List<String> citedIds) {
}