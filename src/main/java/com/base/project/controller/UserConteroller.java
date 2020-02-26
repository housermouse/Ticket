package com.base.project.controller;

import com.alibaba.fastjson.JSONObject;
import com.base.project.entity.User;
import com.base.project.service.UserService;
import com.base.project.util.JsonBackUtil;
import com.base.project.util.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RestController
@RequestMapping("/user")
public class UserConteroller {

    @Autowired
    UserService userService;


    @RequestMapping("/login")
    public JSONObject userLogin(HttpServletRequest request, HttpServletResponse response) {
        User user = new User();
        user.setUserName(request.getParameter("userName"));
        user.setPassWords(request.getParameter("passWords"));
        User result = userService.CheckLogin(user);
        JSONObject Response = new JSONObject();
        Response.put("user",user);
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
        User user = new User();
        user.setPassWords(request.getParameter("passWords"));
        user.setPickName(request.getParameter("pickName"));
        user.setUserName(request.getParameter("userName"));
        JSONObject jsonObject = new JSONObject();
        user = userService.UserRegister(user);
       if(user!=null){ //插入成功返回true 失败返回false 根据返回值跳页面
           jsonObject.put("user",user);
           return JsonBackUtil.success(jsonObject);
       }else {
           return JsonBackUtil.fail(jsonObject);
       }
    }
}
