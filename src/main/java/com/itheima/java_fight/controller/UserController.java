package com.itheima.java_fight.controller;

import com.itheima.java_fight.annotation.RequireAdmin;
import com.itheima.java_fight.common.Result;
import com.itheima.java_fight.dto.LoginDTO;
import com.itheima.java_fight.dto.UpdateRoleDTO;
import com.itheima.java_fight.pojo.PageResult;
import com.itheima.java_fight.pojo.User;
import com.itheima.java_fight.service.UserService;
import com.itheima.java_fight.util.JwtUtil;
import com.itheima.java_fight.vo.UserVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil){
       this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/findAll")
    public Result<List<UserVO>> findAll(){
        return Result.success(userService.findAll().stream().map(UserVO::from).toList());
    }

    @GetMapping("/findById/{id}")
    public Result<UserVO> findById(@PathVariable @Min(1)Long id){
        return Result.success(UserVO.from(userService.findById(id)));
    }

    @RequireAdmin
    @PostMapping("/insert")
    public Result<UserVO> insert(@Valid @RequestBody User user){
        return Result.success(UserVO.from(userService.insert(user)));
    }

    @RequireAdmin
    @PutMapping("/update")
    public Result<UserVO> update(@Valid @RequestBody User user){
        return Result.success(UserVO.from(userService.update(user)));
    }

    @RequireAdmin
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id){
        userService.delete(id);
        return Result.success("删除成功");
    }

    @GetMapping("/users")
    public Result<PageResult<UserVO>>  getPageResult(@RequestParam(value="page")Long page
            ,@RequestParam(value="pageSize") Long pageSize
            ,@RequestParam(value="username",required = false)String username
            ,@RequestParam(value="age",required = false)Integer age
    ) {

        PageResult<User> pageData = userService.selectPage(page,pageSize,age,username);
        List<UserVO> records = pageData.getRecords().stream().map(UserVO::from).toList();
        PageResult<UserVO> result = new PageResult<>(pageData.getTotalPages(),pageData.getTotal(),records);
        return Result.success(result);
    }

    @RequireAdmin
    @DeleteMapping("/deleteBatch")
    public Result<String> deleteBatch(@RequestBody List<Long>ids){
        userService.deleteBatch(ids);
        return Result.success("删除成功");
    }

    @PostMapping("/login")
    public Result<String>login(@Valid @RequestBody LoginDTO loginDTO){
        User user = userService.login(loginDTO.getUsername(),loginDTO.getPassword());
        String token = jwtUtil.generateToken(user);
        return Result.success(token);
    }

    @RequireAdmin
    @PutMapping("/updateRole")
    public Result<String> updateRole(@Valid @RequestBody UpdateRoleDTO dto){
        userService.updateRole(dto.getUserId(), dto.getRole());
        return Result.success("修改成功");
    }


}
