package com.itheima.java_fight.dto;

import jakarta.validation.constraints.NotBlank;

public class ConversationDTO {
    @NotBlank
    private String title;
    public ConversationDTO() {
    }

    public ConversationDTO(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
