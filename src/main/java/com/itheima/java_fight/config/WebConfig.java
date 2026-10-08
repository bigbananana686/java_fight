package com.itheima.java_fight.config;

import com.itheima.java_fight.interceptor.AuthInterceptor;
import com.itheima.java_fight.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final AuthInterceptor authInterceptor;

    public WebConfig(LoginInterceptor loginInterceptor, AuthInterceptor authInterceptor) {
        this.loginInterceptor = loginInterceptor;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/index.html", "/favicon.ico", "/error");

        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**");
    }
}