package com.itheima.java_fight.service;

import com.itheima.java_fight.context.UserContext;
import com.itheima.java_fight.dto.ChatAnswer;
import com.itheima.java_fight.dto.ChatMessage;
import com.itheima.java_fight.exception.BusinessException;
import com.itheima.java_fight.exception.ForbiddenException;
import com.itheima.java_fight.mapper.ConversationMapper;
import com.itheima.java_fight.pojo.Conversation;
import com.itheima.java_fight.pojo.Message;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private final ConversationMapper conversationMapper;
    private final MessageService messageService;
    private final ConversationService conversationService;
    private final DeepSeekClient deepSeekClient;
    private final DocService docService;
    public ChatService(ConversationMapper conversationMapper, MessageService messageService,
                       ConversationService conversationService, DeepSeekClient deepSeekClient, DocService docService){
        this.conversationMapper = conversationMapper;
        this.messageService = messageService;
        this.conversationService = conversationService;
        this.deepSeekClient = deepSeekClient;
        this.docService = docService;
    }

    public Message chat(Long conversationId, String content) {
        Conversation conversation = conversationMapper.findById(conversationId);
        if (conversation == null) {
            throw new BusinessException("数据异常");
        }
        if (!conversation.getUserId().equals(UserContext.getUserId())) {
            throw new ForbiddenException("无权限");
        }
        messageService.append(conversationId, "user", content);

        ChatAnswer answer = deepSeekClient.callModel(conversationId, UserContext.getUserId(), content);
        Message message = messageService.appendReply(conversationId, answer.answer());
        return message;
    }
}