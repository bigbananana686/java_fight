package com.itheima.java_fight.interceptor;

import com.itheima.java_fight.context.UserContext;
import com.itheima.java_fight.exception.UnauthorizedException;
import com.itheima.java_fight.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public LoginInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new UnauthorizedException("未登录或登录已过期");
        }

        String token = auth.substring(7);

        try {
            Claims claims = jwtUtil.parseToken(token);
            UserContext.setUserId(Long.valueOf(claims.getSubject()));
            return true;
        } catch (JwtException e) {
            log.warn(">>> 验签失败: {}", e.getMessage());
            throw new UnauthorizedException("未登录或登录已过期");
        }
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.remove();
    }
}