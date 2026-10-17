package com.itheima.java_fight.service;

import com.itheima.java_fight.ai.DocTools;
import com.itheima.java_fight.dto.ChatAnswer;
import com.itheima.java_fight.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@Service
public class DeepSeekClient {

    private final ChatClient chatClient;

    public DeepSeekClient(ChatClient.Builder builder, ChatMemory chatMemory,
                          VectorStore vectorStore, DocTools docTools) {
        this.chatClient = builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .topK(3)
                                        .similarityThreshold(0.0)
                                        .build())
                                .build())
                .defaultTools(docTools)
                .build();
    }

    public ChatAnswer callModel(Long conversationId, Long userId, String content) {
        try {
            ChatAnswer answer = chatClient.prompt()
                    .user(content)
                    .advisors(a -> a
                            .param(ChatMemory.CONVERSATION_ID, String.valueOf(conversationId))
                            .param(QuestionAnswerAdvisor.FILTER_EXPRESSION, "user_id == '" + userId + "'"))
                    .call()
                    .entity(ChatAnswer.class);
            log.info("chat answer citedIds={}", answer.citedIds());
            return answer;
        } catch (ResourceAccessException e) {
            log.error("DeepSeek 连接失败或超时：{}", e.getMessage());
            throw new BusinessException("AI 服务响应超时，请稍后重试");
        } catch (NonTransientAiException e) {
            log.error("DeepSeek 调用失败（4xx）：{}", e.getMessage());
            throw new BusinessException("AI 服务调用失败");
        } catch (TransientAiException e) {
            log.error("DeepSeek 暂时不可用（超时/5xx）", e);
            throw new BusinessException("AI 服务暂时不可用，请稍后重试");
        }
    }
}