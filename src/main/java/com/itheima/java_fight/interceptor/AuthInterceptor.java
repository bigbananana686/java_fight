package com.itheima.java_fight.interceptor;

import com.itheima.java_fight.annotation.RequireAdmin;
import com.itheima.java_fight.context.UserContext;
import com.itheima.java_fight.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public AuthInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        if(!(handler instanceof HandlerMethod)){
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod)handler;
        if(handlerMethod.getMethodAnnotation(RequireAdmin.class) == null){
            return true;
        }
        userService.checkAdmin(UserContext.getUserId());

        return true;
    }
}