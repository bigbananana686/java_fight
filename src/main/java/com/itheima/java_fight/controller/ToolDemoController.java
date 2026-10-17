package com.itheima.java_fight.controller;

import com.itheima.java_fight.ai.UserTools;
import com.itheima.java_fight.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ToolDemoController {

    private final ChatClient chatClient;

    public ToolDemoController(ChatClient.Builder builder, UserTools userTools) {
        // 排班形状，对照 MemoryDemoController 的 L22：工具焊死，每次请求都带
        this.chatClient = builder
                .defaultTools(userTools)
                .build();
    }

    @GetMapping("/tool-demo")
    public Result<String> toolDemo(@RequestParam("q") String q) {
        String reply = chatClient.prompt()
                .user(q)
                .call()
                .content();
        log.info("tool-demo q={} reply={}", q, reply);
        return Result.success(reply);
    }
}