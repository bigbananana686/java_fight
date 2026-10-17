package com.itheima.java_fight.controller;

import com.itheima.java_fight.common.Result;
import com.itheima.java_fight.dto.CategoryResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ClassifyController {
    private final ChatClient chatClient;

    public ClassifyController(ChatClient.Builder builder){
        this.chatClient = builder.build();
    }

    @GetMapping("/classify")
    public Result<CategoryResult>classify(@RequestParam("text")String text){
        CategoryResult result = chatClient.prompt()
                .user("把下面这句话分类，只能从这四个里选一个：concept / code / error / other。\n" + text)
                .call()
                .entity(CategoryResult.class);
        log.info("classify result = {}",result);
        return Result.success(result);
    }

    @GetMapping("/classify-debug")
    public Result<String> classifyDebug(@RequestParam("text") String text) {
        // 故意不给 schema 指令，用 content() 拿模型自由发挥的原文
        String raw = chatClient.prompt().user(text).call().content();
        try {
            CategoryResult r = new BeanOutputConverter<>(CategoryResult.class).convert(raw);
            return Result.success("parsed ok: " + r);
        } catch (Exception e) {
            return Result.success("raw=" + raw + " || ex=" + e.getClass().getName());
        }
    }

    @GetMapping("/classify-extra")
    public Result<String> classifyExtra() {
        String fakeReply = "{\"category\":\"code\",\"unexpected\":\"x\"}";   // 多给一个你没定义的字段
        try {
            CategoryResult r = new BeanOutputConverter<>(CategoryResult.class).convert(fakeReply);
            return Result.success("parsed ok: " + r);
        } catch (Exception e) {
            return Result.success("ex=" + e.getClass().getName() + " || " + e.getMessage());
        }
    }

}
