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

    public User checkUserLogin(User usre){
        return userMapper.selectOne(usre);
    }
    //插入成功返回true 失败返回false
    public User inSertUser(User user){
        try{
            int num = userMapper.insert(user);
            if(num>0)
                return userMapper.selectOne(user);
            else
                return null;
        }
        catch (Exception e){
            e.printStackTrace();
            return null; //插入失败返回false
        }
    }
}
