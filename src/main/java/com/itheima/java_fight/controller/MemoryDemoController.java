package com.itheima.java_fight.controller;

import com.itheima.java_fight.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MemoryDemoController {

    private final ChatClient chatClient;

    public MemoryDemoController(ChatClient.Builder builder, ChatMemory chatMemory) {
        // 时机一：装卸工进编制，这个 client 每次调用都带记忆
        // chatMemory 不用自己造——任务 0 核过，自动配置直接注入
        this.chatClient = builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @GetMapping("/memory-demo")
    public Result<String> memoryDemo(@RequestParam("conversationId") String conversationId,
                                     @RequestParam("content") String content) {
        // 会话 id 每次都不一样 → 走 prompt 链传（口诀第三条）
        String reply = chatClient.prompt()
                .user(content)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
        log.info("memory-demo conv={} content={} reply={}", conversationId, content, reply);
        return Result.success(reply);
    }
}