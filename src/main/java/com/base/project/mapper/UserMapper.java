package com.base.project.mapper;


import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.base.project.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface  UserMapper extends BaseMapper<User> {
    @Select("SELECT *FROM t_user where username where user_name=#{username}")
    public User checkUserLogin(String username);

}
