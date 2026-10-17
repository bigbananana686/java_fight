package com.itheima.java_fight.ai;

import com.itheima.java_fight.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserTools {

    private final UserService userService;

    public UserTools(UserService userService) {
        this.userService = userService;
    }

    @Tool(description = "统计系统用户表(tb_user)的总行数，返回用户总数，整数")
    public int countUsers() {
        int count = userService.count();
        log.info("countUsers invoked -> {}", count);
        return count;
    }
}