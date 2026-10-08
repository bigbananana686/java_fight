package com.itheima.java_fight.service;

import com.itheima.java_fight.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private com.itheima.java_fight.mapper.UserMapper userMapper;


    // 测试方法写这下面
    @Test
    void testUserSameByUsername(){
        User user = new User(null,"test-integration-01",66,null,"123456","USER");
        User user1 = userService.insert(user);
        User found = userService.findById(user1.getId());
        assertNotNull(found);
        assertEquals("test-integration-01",found.getUsername());
    }
    @Test
    void findByUsernameShouldReturnUserWithHashedPassword() {
        String username = "test-findby-" + System.currentTimeMillis();
        String rawPassword = "test-pwd-01";

        User user = new User(null, username, 30, null, rawPassword,"USER");
        userService.insert(user);

        User found = userMapper.findByUsername(username);

        assertNotNull(found);
        assertEquals(username, found.getUsername());
        assertTrue(found.getPassword().startsWith("$2a$"));
        assertTrue(new BCryptPasswordEncoder().matches(rawPassword, found.getPassword()));
    }

}