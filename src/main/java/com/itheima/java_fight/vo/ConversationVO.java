package com.itheima.java_fight.vo;

import com.itheima.java_fight.pojo.Conversation;

import java.time.LocalDateTime;

public class ConversationVO {

    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ConversationVO() {
    }

    public ConversationVO(Long id, String title, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }
    public static ConversationVO from(Conversation conversation){
        Long id = conversation.getId();
        String title = conversation.getTitle();
        LocalDateTime createdAt = conversation.getCreatedAt();
        LocalDateTime updatedAt = conversation.getUpdatedAt();
        ConversationVO conversationVO = new ConversationVO(id,title,createdAt,updatedAt);
        return conversationVO;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
