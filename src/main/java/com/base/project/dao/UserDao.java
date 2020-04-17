package com.base.project.dao;

import com.base.project.entity.User;
import com.base.project.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class UserDao {


    @Autowired
    private UserMapper userMapper;

    public User checkUserLogin(User usre) {
        return userMapper.checkUserLogin(usre.getUserName());
    }

    //插入成功返回true 失败返回false
    public int insert(User user) {
        return userMapper.insert(user);
    }
}
