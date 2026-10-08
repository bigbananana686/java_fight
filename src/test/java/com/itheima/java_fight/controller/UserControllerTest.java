package com.itheima.java_fight.controller;

import com.itheima.java_fight.pojo.User;
import com.itheima.java_fight.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;import com.jayway.jsonpath.JsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private String token;

    @BeforeEach
    void setUp() {
        User user = new User(21L, "test-bcrypt-01", 20, null, null, "ADMIN");
        token = jwtUtil.generateToken(user);
    }

    @Test
    void findByIdShouldReturnFormatErrorWhenIdIsNotNumber() throws Exception {
        mockMvc.perform(get("/findById/abc").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("id 格式错误"));
    }

    @Test
    void insertUserShouldReturnAgeErrorWhenageIsToBig() throws Exception{
        String body = """
    {"username":"","age":999}
    """;
        mockMvc.perform(post("/insert").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value(org.hamcrest.Matchers.containsString("username")));
    }

    @Test
    void insertShouldReturnCodeOneWhenDataIsValid() throws Exception{
        String body = """
        {"username":"test-mockmvc-01","age":36,"password":"123456"}
        """;
        mockMvc.perform(post("/insert").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

    }
    @Test
    void findAllShouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/findAll"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("未登录或登录已过期"));
    }

    @Test
    void findAllShouldReturnDataWhenTokenIsValid() throws Exception {
        mockMvc.perform(get("/findAll")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    void deleteShouldReturn403WhenUserIsNotAdmin() throws Exception {
        User normalUser = new User(37L, "test-user-01", 22, null, null, "USER");
        String userToken = jwtUtil.generateToken(normalUser);

        mockMvc.perform(delete("/delete/999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("无权限执行该操作"));
    }
    @Test
    void updateShouldReturnUpdatedUser() throws Exception {
        String insertBody = """
                {"username":"test-mvc-update-01","age":18,"password":"pwd123"}
                """;
        String insertResp = mockMvc.perform(post("/insert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(insertBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn().getResponse().getContentAsString();

        Number idNum = JsonPath.read(insertResp, "$.data.id");
        Long id = idNum.longValue();

        String updateBody = """
                {"id":%d,"username":"test-mvc-update-01","age":30}
                """.formatted(id);

        mockMvc.perform(put("/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.age").value(30))
                .andExpect(jsonPath("$.data.username").value("test-mvc-update-01"));
    }

}