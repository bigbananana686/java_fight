package com.itheima.java_fight.service;

import com.itheima.java_fight.context.UserContext;
import com.itheima.java_fight.exception.BusinessException;
import com.itheima.java_fight.exception.ForbiddenException;
import com.itheima.java_fight.mapper.ConversationMapper;
import com.itheima.java_fight.mapper.MessageMapper;
import com.itheima.java_fight.pojo.Conversation;
import com.itheima.java_fight.pojo.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    public MessageService(MessageMapper messageMapper,ConversationMapper conversationMapper){
        this.messageMapper = messageMapper;
        this.conversationMapper = conversationMapper;
    }

    public List<Message>listByConversationId(Long conversationId){
        Conversation conversation = conversationMapper.findById(conversationId);
        if (conversation == null){
            throw new BusinessException("数据异常");
        }
        Long userId = UserContext.getUserId();
        if (!userId.equals(conversation.getUserId())){
            throw new ForbiddenException("无权限");
        }else{
            return messageMapper.findByConversationId(conversationId);
        }
    }

    @Transactional
    public Message appendReply(Long conversationId, String content) {
        Message message = append(conversationId, "assistant", content);
        conversationMapper.updateUpdatedAt(conversationId, LocalDateTime.now());
        return message;
    }

    public Message append(Long conversationId,String role,String content){
        Message message = new Message(null,conversationId,role,content, LocalDateTime.now());
        messageMapper.insert(message);
        return message;

    }



}
