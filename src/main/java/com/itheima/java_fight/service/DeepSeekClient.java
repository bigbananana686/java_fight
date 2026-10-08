package com.itheima.java_fight.service;

import com.itheima.java_fight.dto.ChatMessage;
import com.itheima.java_fight.exception.BusinessException;
import org.springframework.ai.chat.messages.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

@Slf4j
@Service
public class DeepSeekClient {



    private final ChatClient chatClient;

    public DeepSeekClient(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String callModel(List<ChatMessage> chatMessages) {
        List<Message>messages = chatMessages.stream().map(m->toMessage(m)).toList();
        try {
            return chatClient.prompt()
                    .messages(messages)
                    .call()
                    .content();
        } catch (ResourceAccessException e) {
            log.error("DeepSeek 连接失败或超时：{}", e.getMessage());
            throw new BusinessException("AI 服务响应超时，请稍后重试");
        }
        catch (
    NonTransientAiException e) {
        log.error("DeepSeek 调用失败（4xx）：{}", e.getMessage());
        throw new BusinessException("AI 服务调用失败");
    } catch (
    TransientAiException e) {
        log.error("DeepSeek 暂时不可用（超时/5xx）", e);
        throw new BusinessException("AI 服务暂时不可用，请稍后重试");
    }
    }

    private Message toMessage(ChatMessage chatMessage){
        if ("assistant".equals(chatMessage.role())){
            return new AssistantMessage(chatMessage.content());
        }
        if ("system".equals(chatMessage.role())){
            return new SystemMessage(chatMessage.content());
        }
        return new UserMessage(chatMessage.content());
    }

}