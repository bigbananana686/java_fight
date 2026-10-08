package com.itheima.java_fight.controller;


import com.itheima.java_fight.common.Result;
import com.itheima.java_fight.dto.ConversationDTO;
import com.itheima.java_fight.pojo.Conversation;
import com.itheima.java_fight.service.ConversationService;
import com.itheima.java_fight.vo.ConversationVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
public class ConversationController {

    private final ConversationService conversationservice;

    public ConversationController(ConversationService conversationservice){
        this.conversationservice = conversationservice;
    }

    @PostMapping("/conversation")
    public Result<ConversationVO> create(@RequestBody ConversationDTO conversationDTO){
        Conversation conversation = conversationservice.create(conversationDTO.getTitle());
        return Result.success(ConversationVO.from(conversation));
    }

    @GetMapping("/conversation/list")
    public Result<List<ConversationVO>>listMine(){
        List<ConversationVO>list = conversationservice.listMine().stream().map(ConversationVO::from).toList();
        return Result.success(list);
    }

    @DeleteMapping("/conversation/{id}")
    public Result<String> deleteConversation(@PathVariable Long id){
        conversationservice.delete(id);
        return Result.success("删除成功");
    }
}
