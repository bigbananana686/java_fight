package com.itheima.java_fight.pojo;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class User {

    private Long id;
    @NotBlank(message="用户名不为空")
    @Size(min=3,max=20,message="用户名得在3-20之间")
    private String username;
    @NotNull(message="年龄不能为空")
    @Max(value = 120,message = "年龄超出合理范围")
    @Min(value=0,message="年龄不能为负")
    private Integer age;
    private LocalDateTime createdAt;
    private String password;
    private String role;
    public User(Long id, String username, Integer age, LocalDateTime createdAt, String password, String role){
        this.id = id;
        this.username = username;
        this.age = age;
        this.createdAt = createdAt;
        this.password = password;
        this.role = role;
    }

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAge() {
        return age;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
