package com.itheima.java_fight.service;

import com.itheima.java_fight.exception.BusinessException;
import com.itheima.java_fight.mapper.UserMapper;
import com.itheima.java_fight.pojo.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class UserServiceTest{

    @Mock
    private UserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @InjectMocks
    private UserService userService;


    @Test
    void deleteBatchShouldThrowWhenIdsIsNull(){
        assertThrows(BusinessException.class,()->userService.deleteBatch(null));
    }

    @Test
    void deleteBatchShouldThrowWhenIdsIsEmpty(){
        assertThrows(BusinessException.class,()->userService.deleteBatch(Arrays.asList()));
    }

    @Test
    void deleteBatchShouldThrowWhenIdshasNull(){
        assertThrows(BusinessException.class,()->userService.deleteBatch(Arrays.asList(1L,null,3L)));
    }

    @Test
    void deleteBatchShouldThrowWhenIdhasmore(){
        assertThrows(BusinessException.class,()->userService.deleteBatch(Arrays.asList(1L,1L,2L)));
    }

    @Test
    void deleteBatchReturn3(){
        List<Long> ids = Arrays.asList(1L, 2L, 3L);

        when(userMapper.deleteBatch(ids)).thenReturn(ids.size());
        userService.deleteBatch(ids);
        verify(userMapper).deleteBatch(ids);
    }

    @Test
    void deleteBatchThrowBusinessException(){
        List<Long> ids = Arrays.asList(1L, 2L, 3L);

        when(userMapper.deleteBatch(ids)).thenReturn(0);
        assertThrows(BusinessException.class,()->userService.deleteBatch(ids));
    }

    @Test
    void insertShouldReturnUserFromDatabase(){
        User user = new User(null, "test-same-01", 30, null, "raw-pwd-01","USER");
        User fromDb = new User(1L, "test-same-01", 30, LocalDateTime.now(), "$2a$10$fakeHashForTest","USER");

        when(userMapper.insert(user)).thenReturn(1);
        when(userMapper.findById(any())).thenReturn(fromDb);

        User result = userService.insert(user);

        assertSame(fromDb, result);
    }

    @Test
    void InsertException(){
        User user = new User();
        when(userMapper.insert(user)).thenReturn(0);
        assertThrows(BusinessException.class,()->userService.insert(user));
    }






}

