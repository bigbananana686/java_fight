package com.itheima.java_fight.service;

import com.itheima.java_fight.context.UserContext;
import com.itheima.java_fight.exception.BusinessException;
import com.itheima.java_fight.exception.ForbiddenException;
import com.itheima.java_fight.mapper.ConversationMapper;
import com.itheima.java_fight.mapper.MessageMapper;
import com.itheima.java_fight.pojo.Conversation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversationService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    public ConversationService(ConversationMapper conversationMapper,MessageMapper messageMapper){
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
    }
    public Conversation create(String title){
        Long userId = UserContext.getUserId();
        Conversation conversation = new Conversation(null,userId,title,LocalDateTime.now(),LocalDateTime.now());
        int affectedrow = conversationMapper.insert(conversation);
        if (affectedrow == 1) {
            conversation = conversationMapper.findById(conversation.getId());
            if (conversation==null){
                throw new ForbiddenException("权限不足");
            }else{
                return conversation;
            }
        }else{
            throw new BusinessException("插入失败");
        }
        }

    public List<Conversation> listMine(){
        Long userId = UserContext.getUserId();
        List<Conversation>list = conversationMapper.findByUserId(userId);
        return list;

    }

    @Transactional
    public void delete(Long id){

        Long userId = UserContext.getUserId();
        int afftedrow = conversationMapper.delete(id,userId);
        if (afftedrow ==0){
            throw new ForbiddenException("删除失败");
        }
        messageMapper.deleteByConversationId(id);
    }

    public void touch(Long conversationId){
        conversationMapper.updateUpdatedAt(conversationId,LocalDateTime.now());
    }


}
