package com.itheima.java_fight.ai;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("0.4 已通过，冒烟退役；需要重跑时删掉这一行")
@SpringBootTest
class DeepSeekSmokeTest {

    @Autowired
    private DeepSeekChatModel chatModel;

    @Test
    void smoke() {
        ChatResponse response = chatModel.call(new Prompt("Reply with the single word: OK"));

        ChatResponseMetadata meta = response.getMetadata();
        String model = meta.getModel();
        String content = response.getResult().getOutput().getText();

        System.out.println(">>> SMOKE model=" + model);
        System.out.println(">>> SMOKE content=" + content);
        System.out.println(">>> SMOKE usage=" + meta.getUsage());

        assertNotNull(content);
    }
}