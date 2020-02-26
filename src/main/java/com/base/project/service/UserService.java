package com.base.project.service;


import com.base.project.dao.UserDao;
import com.base.project.entity.User;
import com.base.project.util.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class UserService {
    @Autowired
    private UserDao userDao;

    public User CheckLogin(User user){
        return userDao.checkUserLogin(user);
    }
    //插入成功返回true 失败返回false
    public User UserRegister(User user){
        return userDao.inSertUser(user);
    }
}
