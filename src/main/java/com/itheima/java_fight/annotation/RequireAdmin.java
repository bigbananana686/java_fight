package com.itheima.java_fight.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 贴在 Controller 方法上：只有 role = ADMIN 的登录用户才能调用。
 * 注意它身上没有任何逻辑，它只是一个「标记」。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAdmin {
}