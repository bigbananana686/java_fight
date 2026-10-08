package com.itheima.java_fight.util;

import com.itheima.java_fight.pojo.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final long EXPIRE_MILLIS = 30 * 60 * 1000L;   // 30 分钟

    // HS256 要求密钥不短于 256 位，也就是 32 字节 / 32 个字符
    private final SecretKey key;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 签发：把用户 id 放进 sub，用户名放进自定义 claim */
    public String generateToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXPIRE_MILLIS))
                .signWith(key)
                .compact();
    }

    /** 验签 + 解析。签名不对或已过期都会抛 JwtException，任务 6 用它拦请求 */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}