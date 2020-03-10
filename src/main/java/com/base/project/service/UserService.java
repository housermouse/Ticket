package com.base.project.service;


import com.base.project.dao.UserDao;
import com.base.project.entity.User;
import com.base.project.util.SessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Slf4j
public class UserService {
    @Autowired
    private UserDao userDao;

    public User CheckLogin(User user) {
        return userDao.checkUserLogin(user);
    }

    //插入成功返回true 失败返回false
    public User UserRegister(User user) {
        User result = null;
        try{
            User loginUser = userDao.checkUserLogin(user);
            if(loginUser!= null){
                result = loginUser;
            }else {
                userDao.insert(user);
                result = user;
            }
        }catch (Exception e){
            log.error("注册错误"+e.getMessage());
        }
        return result;
    }
}
