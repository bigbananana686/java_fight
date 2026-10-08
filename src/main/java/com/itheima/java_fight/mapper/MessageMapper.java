package com.itheima.java_fight.mapper;

import com.itheima.java_fight.pojo.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MessageMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO tb_message(conversation_id, role, content, created_at) VALUES (#{conversationId}, #{role}, #{content}, #{createdAt})")
    int insert(Message message);

    @Select("SELECT id, conversation_id, role, content, created_at FROM tb_message WHERE conversation_id = #{conversationId} ORDER BY id")
    List<Message> findByConversationId(@Param("conversationId") Long conversationId);

    @Delete("DELETE FROM tb_message WHERE conversation_id = #{conversationId} ")
    int deleteByConversationId(@Param("conversationId") Long conversationId);
}
