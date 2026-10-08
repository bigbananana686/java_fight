package com.itheima.java_fight.vo;

import com.itheima.java_fight.pojo.Message;

import java.time.LocalDateTime;

public class MessageVO {

    private Long id;
    private String role;
    private String content;
    private LocalDateTime createdAt;

    public MessageVO() {
    }

    public static MessageVO from(Message message){
        Long id = message.getId();
        String role = message.getRole();
        String content = message.getContent();
        LocalDateTime createdAt = message.getCreatedAt();
        MessageVO messageVO = new MessageVO(id,role,content,createdAt);
        return  messageVO;
    }

    public MessageVO(Long id, String role, String content, LocalDateTime createdAt) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
