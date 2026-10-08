package com.itheima.java_fight.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class OllamaEmbeddingSmokeTest {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private DeepSeekChatModel chatModel;

    @Test
    void smoke() {
        // 钉死 spring.ai.model.chat=deepseek 之后，DeepSeek 的对话模型必须还在
        assertNotNull(chatModel, "DeepSeekChatModel 应该还在（对话模型这次没换）");

        // 故意用中文：裸协议那步验不了中文（PowerShell 传请求体有编码坑），
        // 这一层走 Jackson，UTF-8 是它自己管的，中文在这里验最干净
        String zh = "缓存穿透是指查询一个数据库中不存在的数据，导致每次请求都打到数据库";
        float[] v = embeddingModel.embed(zh);

        double sum = 0;
        for (float f : v) {
            sum += (double) f * f;
        }

        System.out.println(">>> EMBED class  = " + embeddingModel.getClass().getName());
        System.out.println(">>> EMBED dim    = " + v.length);
        System.out.println(">>> EMBED first5 = " + v[0] + ", " + v[1] + ", " + v[2] + ", " + v[3] + ", " + v[4]);
        System.out.println(">>> EMBED norm   = " + Math.sqrt(sum));

        assertEquals(1024, v.length, "qwen3-embedding:0.6b 的 embedding length 是 1024（来自 ollama show）");
        assertTrue(Math.abs(Math.sqrt(sum) - 1.0) < 1e-3, "Ollama 返回的应当是 L2 归一化向量");
    }
}