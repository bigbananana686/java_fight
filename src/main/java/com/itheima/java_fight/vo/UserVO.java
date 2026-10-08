package com.itheima.java_fight.vo;

import com.itheima.java_fight.pojo.User;

import java.time.LocalDateTime;

public class UserVO {

    private Long id;
    private String username;
    private Integer age;
    private LocalDateTime createdAt;
    private String role;

    public UserVO(Long id, String username, Integer age, LocalDateTime createdAt, String role) {
        this.id = id;
        this.username = username;
        this.age = age;
        this.createdAt = createdAt;
        this.role = role;
    }

    public UserVO() {
    }

    public static UserVO from(User user){
        Long id = user.getId();
        String username = user.getUsername();
        Integer age = user.getAge();
        LocalDateTime createdAt = user.getCreatedAt();
        String role = user.getRole();
        UserVO userVO = new UserVO(id,username,age,createdAt,role);
        return userVO;
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

    public void setAge(Integer age) {
        this.age = age;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}