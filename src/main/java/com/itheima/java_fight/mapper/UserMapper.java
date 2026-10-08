package com.itheima.java_fight.mapper;


import com.itheima.java_fight.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT id, username, age, created_at,password,role FROM tb_user")
    List<User> findAll();

    @Select("SELECT id, username, age, created_at,password,role FROM tb_user WHERE id = #{id}")
    User findById(@Param("id")Long id);

    @Options(useGeneratedKeys = true,keyProperty = "id")
    @Insert("INSERT INTO tb_user(username, age,password) VALUES (#{username}, #{age},#{password})")
    int insert(User user);

    @Update("UPDATE tb_user set username = #{username} , age = #{age} where id = #{id} ")
    int update(User user);

    @Delete("DELETE FROM tb_user WHERE id = #{id}")
    int delete(@Param("id") Long id);

    List<User> selectPage(@Param("offset")Long offset,@Param("size")Long size,@Param("username")String username,@Param("age")Integer age);

    Long getUserNumber(@Param("username")String username,@Param("age")Integer age);

    public int deleteBatch(@Param("ids")List<Long>ids);

    @Select("SELECT id, username, age, created_at, password,role FROM tb_user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    @Update("UPDATE tb_user SET role = #{role} WHERE id = #{id}")
    int updateRole(@Param("id") Long id, @Param("role") String role);
}


