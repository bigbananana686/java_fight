package com.itheima.java_fight.service;


import com.itheima.java_fight.exception.BusinessException;
import com.itheima.java_fight.exception.ForbiddenException;
import com.itheima.java_fight.mapper.UserMapper;
import com.itheima.java_fight.pojo.PageResult;
import com.itheima.java_fight.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


@Slf4j
@Service
public class UserService {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final String USER_KEY_PREFIX = "user:";
    private static final Duration USER_CACHE_TTL = Duration.ofMinutes(30);
    private static final Duration NULL_CACHE = Duration.ofMinutes(2);
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final Duration LOGIN_FAIL_TTL  = Duration.ofMinutes(5);
    private static final int      MAX_LOGIN_FAIL  = 5;

    public UserService(UserMapper userMapper, BCryptPasswordEncoder passwordEncoder, StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<User>findAll(){
        return userMapper.findAll();

    }


    public User insert(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if(userMapper.insert(user) == 1){
            return userMapper.findById(user.getId());
        }else{
            throw new BusinessException("新增用户失败");
        }

    }


    public User findById(Long id){
        if (id == null || id <= 0) {
            throw new BusinessException("id 必须为正数");
        }

        String key = USER_KEY_PREFIX + id;          // 数据键  user:21
        String lockKey = "lock:user:" + id;         // 锁键    lock:user:21

        for (int attempt = 0; attempt < 3; attempt++) {

            // ① 查缓存（锁外）—— 命中的请求在这儿就返回，一步都不碰锁
            String cached = stringRedisTemplate.opsForValue().get(key);
            if (cached != null) {
                if (cached.isEmpty()) {
                    return null;
                }
                try {
                    return objectMapper.readValue(cached, User.class);
                } catch (JacksonException e) {
                    log.warn("缓存反序列化失败，回源查库。key={}", key, e);
                }
            }

            // ② 抢锁（锁外）—— 只有确认没命中才来抢
            Boolean got = stringRedisTemplate.opsForValue()
                            .setIfAbsent(lockKey, "1", Duration.ofSeconds(10));

            if (Boolean.TRUE.equals(got)) {
                try {
                    // ③ 双检 —— 排队这段时间里，别人可能已经填好了
                    String cachedAgain = stringRedisTemplate.opsForValue().get(key);
                    if (cachedAgain != null) {
                        if (cachedAgain.isEmpty()) {
                            return null;
                        }
                        try {
                            return objectMapper.readValue(cachedAgain, User.class);
                        } catch (JacksonException e) {
                            log.warn("缓存反序列化失败，回源查库。key={}", key, e);
                        }
                    }

                    // ④ 查库
                    User user = userMapper.findById(id);

                    // ⑤ 写回
                    if (user == null) {
                        stringRedisTemplate.opsForValue().set(key, "", NULL_CACHE);
                    } else {
                        try {
                            String json = objectMapper.writeValueAsString(user);
                            stringRedisTemplate.opsForValue().set(key, json, USER_CACHE_TTL);
                        } catch (JacksonException e) {
                            log.warn("写回缓存失败，本次不回填。key={}", key, e);
                        }
                    }
                    return user;
                } finally {
                    // ⑥ 放锁 —— 无论怎么出去，锁都要还
                    stringRedisTemplate.delete(lockKey);
                }
            }

            // ⑦ 没抢到：等一会儿，回到 ①
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // ⑧ 三轮都没抢到（极端情况）：不抢锁，直接查库，保证请求能出去
        return userMapper.findById(id);
    }



    public User update(User user){


        if(userMapper.update(user) != 1){
            throw new BusinessException("更新用户失败");
        }else {
            String key = USER_KEY_PREFIX+user.getId();
            stringRedisTemplate.delete(key);
            User temp = userMapper.findById(user.getId());
            return temp;

        }

    }

    public void delete(Long id){
        if(userMapper.delete(id) != 1){
            throw new BusinessException("用户删除失败");
        }
        String key = USER_KEY_PREFIX+id;
        stringRedisTemplate.delete(key);
    }

    public PageResult<User> selectPage(Long page, Long pagesize,Integer age,String username){
        Long total = userMapper.getUserNumber(username,age);
        Long totalPages;
        if (total ==0){
            totalPages = 0L;
        }else{
            totalPages = (total+pagesize-1)/pagesize;
        }
        Long offset = (page-1L)*pagesize;
        List<User>records = userMapper.selectPage(offset,pagesize,username,age);
        PageResult<User> pageresult= new PageResult<>(totalPages,total,records);
        return pageresult;
    }

    @Transactional
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("参数异常");
        }

        boolean hasNull = ids.contains(null);
        boolean hasDuplicate =
                new HashSet<>(ids).size() != ids.size();

        if (hasNull) {
            throw new BusinessException("参数异常");
        }

        if (hasDuplicate) {
            throw new BusinessException("参数异常");
        }

        int affectedRows = userMapper.deleteBatch(ids);
        if (affectedRows!=ids.size()){
            throw new BusinessException("批量删除用户失败");
        }

        List<String> keys = new ArrayList<>();
        for (Long id : ids) {
            keys.add(USER_KEY_PREFIX + id);
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    stringRedisTemplate.delete(keys);
                }
            });
        } else {
            stringRedisTemplate.delete(keys);
        }
    }

    public User login(String username, String rawPassword) {
        String key = LOGIN_FAIL_PREFIX + username;        // login:fail:test-user-01

        // ① 进门先判断。它在 increment 之前 —— 被拦住的请求不涨计数（你自己说的）
        String count = stringRedisTemplate.opsForValue().get(key);
        if (count != null && Integer.parseInt(count) >= MAX_LOGIN_FAIL) {
            throw new BusinessException("失败次数过多，请 5 分钟后再试");
        }

        // ② 验身份。原来那两段 if 合并成一次判断，结果一样
        User user = userMapper.findByUsername(username);
        boolean ok = user != null && passwordEncoder.matches(rawPassword, user.getPassword());

        if (!ok) {
            Long c = stringRedisTemplate.opsForValue().increment(key);
            if (c == 1||c>=MAX_LOGIN_FAIL){
                stringRedisTemplate.expire(key,LOGIN_FAIL_TTL);
            }
            throw new BusinessException("用户名或密码错误");
        }

        // ④ 成功：把欠账清零
        stringRedisTemplate.delete(key);
        return user;
    }
    public void checkAdmin(Long userId){
        User user = userMapper.findById(userId);
        if(user == null || !"ADMIN".equals(user.getRole())){
            throw new ForbiddenException("无权限执行该操作");
        }
    }

    public void updateRole(Long userId, String role){
        if(!"ADMIN".equals(role) && !"USER".equals(role)){

            throw new BusinessException("角色只能是 ADMIN 或 USER");
        }
        if(userMapper.updateRole(userId, role) != 1){
            throw new BusinessException("角色修改失败");
        }
        String key = USER_KEY_PREFIX+userId;
        stringRedisTemplate.delete(key);
    }



}
