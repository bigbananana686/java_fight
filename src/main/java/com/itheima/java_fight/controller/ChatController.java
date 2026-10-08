package com.itheima.java_fight.controller;

import com.itheima.java_fight.common.Result;
import com.itheima.java_fight.dto.ChatDTO;
import com.itheima.java_fight.pojo.Message;
import com.itheima.java_fight.service.ChatService;
import com.itheima.java_fight.vo.MessageVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public Result<MessageVO> chat(@RequestBody ChatDTO chatDTO) {
        Message reply = chatService.chat(chatDTO.getConversationId(), chatDTO.getContent());
        return Result.success(MessageVO.from(reply));
    }
}
