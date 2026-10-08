package com.itheima.java_fight.service;

import com.itheima.java_fight.context.UserContext;
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

    private static final int MAX_HISTORY_MESSAGES = 20;
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

    public Message chat(Long conversationId,String content) {
        Conversation conversation = conversationMapper.findById(conversationId);
        if (conversation == null){
            throw new BusinessException("数据异常");
        }
        if (!conversation.getUserId().equals(UserContext.getUserId())){
            throw new ForbiddenException("无权限");
        }
        messageService.append(conversationId,"user",content);

        List<Message> history = messageService.listByConversationId(conversationId);
        int from = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
        List<ChatMessage> messages = new ArrayList<>(
                history.subList(from, history.size()).stream()
                        .map(m -> new ChatMessage(m.getRole(), m.getContent()))
                        .toList()
        );
        List<String> context = docService.retrieve(content);
        if (!context.isEmpty()) {
            messages.add(0, new ChatMessage("system", "以下是相关资料，回答时优先参考：\n" + String.join("\n\n", context)));
        }
        String reply = deepSeekClient.callModel(messages);
        Message message = messageService.appendReply(conversationId, reply);
        return message;
    }
}