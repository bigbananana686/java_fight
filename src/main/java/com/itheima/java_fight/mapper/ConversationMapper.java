package com.itheima.java_fight.mapper;


import com.itheima.java_fight.pojo.Conversation;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ConversationMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO tb_conversation(user_id, title, created_at, updated_at) VALUES (#{userId}, #{title}, #{createdAt}, #{updatedAt})")
    int insert(Conversation conversation);

    @Select("SELECT id, user_id, title, created_at, updated_at FROM tb_conversation WHERE id = #{id}")
    Conversation findById(@Param("id") Long id);

    @Select("SELECT id, user_id, title, created_at, updated_at FROM tb_conversation WHERE user_id = #{userId} ORDER BY updated_at DESC")
    List<Conversation> findByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM tb_conversation WHERE id = #{id} AND user_id = #{userId}")
    int delete(@Param("id") Long id,@Param("userId")Long userId);

    @Update("UPDATE tb_conversation SET title = #{title}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Conversation conversation);

    @Update("UPDATE tb_conversation SET updated_at = #{updatedAt} where id = #{id} ")
    void updateUpdatedAt(@Param("id")Long conversationId, @Param("updatedAt")LocalDateTime now);
}
