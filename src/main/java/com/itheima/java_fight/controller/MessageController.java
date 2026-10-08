package com.itheima.java_fight.controller;

import com.itheima.java_fight.common.Result;
import com.itheima.java_fight.pojo.Message;
import com.itheima.java_fight.service.MessageService;
import com.itheima.java_fight.vo.MessageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MessageController {

    private final MessageService messageService;

    public  MessageController (MessageService messageService){
        this.messageService = messageService;
    }

    @GetMapping("/message/list")
    public Result<List<MessageVO>> findByConversationId(@RequestParam Long conversationId){
    List<MessageVO>list = messageService.listByConversationId(conversationId).stream().map(MessageVO::from).toList();
    return Result.success(list);
    }

}
