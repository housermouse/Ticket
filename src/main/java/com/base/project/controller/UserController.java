package com.base.project.controller;

import com.alibaba.fastjson.JSONObject;
import com.base.project.entity.User;
import com.base.project.service.UserService;
import com.base.project.util.JsonBackUtil;
import com.base.project.util.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.lang3.StringUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author ASUS
 */
@Component
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;


    @RequestMapping("/login")
    public JSONObject userLogin(HttpServletRequest request, HttpServletResponse response) {
        User user = new User();
        JSONObject Response = new JSONObject();
        String passWords = request.getParameter("passWords");
        String userName = request.getParameter("userName");
        if(StringUtils.isBlank(passWords)||StringUtils.isBlank(userName)){
            Response.put("reamrk","参数传入不正确");
            return JsonBackUtil.fail(Response);
        }
        user.setUserName(userName);
        user.setPassWords(passWords);
        User result = userService.CheckLogin(user);

        Response.put("user",result);
        if(result!=null){
          SessionUtils.setUser(request,user);
          return JsonBackUtil.success(Response);
        }
        else {
            return JsonBackUtil.fail(Response);
        }

    }


    @RequestMapping("/logout")
    public JSONObject userLogout(HttpServletRequest request, HttpServletResponse response) {
        User user = SessionUtils.getUser(request);
        if(user!=null){
            SessionUtils.removeUser(request);
        }
        request.setAttribute("userInfo",new JSONObject());
        return JsonBackUtil.success(new JSONObject());
    }

    @RequestMapping("/register")
    public JSONObject UserRegister(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jsonObject = new JSONObject();
        User user = new User();
        String passWords = request.getParameter("passWords");
        String nickName = request.getParameter("nickName");
        String userName = request.getParameter("userName");
        if(StringUtils.isBlank(passWords)||StringUtils.isBlank(nickName)||StringUtils.isBlank(userName)){
            jsonObject.put("reamrk","参数传入不正确");
            return JsonBackUtil.fail(jsonObject);
        }
        user.setNickName(nickName);
        user.setPassWords(passWords);
        user.setUserName(userName);

        user = userService.UserRegister(user);
       if(user!=null){ //插入成功返回true 失败返回false 根据返回值跳页面
           jsonObject.put("user",user);
           jsonObject.put("status",1);
           return JsonBackUtil.success(jsonObject);
       }else {
           return JsonBackUtil.fail(jsonObject);
       }
    }
}
