package com.base.project.util;




import com.base.project.entity.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 *
 * Session 工具类
 *
 */
public final class SessionUtils {


    private static final String SESSION_USER = "session_user";
    private static final int MAX_TIME = 1800; //半小时





    /**
     * 设置session的值
     *
     * @param request
     * @param key
     * @param value
     */
    public static void setAttr(HttpServletRequest request, String key, Object value) {
        request.getSession(true).setAttribute(key, value);
        request.getSession(true).setMaxInactiveInterval(MAX_TIME);

    }


    /**
     * 获取session的值
     *
     * @param request
     * @param key
     */
    public static Object getAttr(HttpServletRequest request, String key) {
        return request.getSession(true).getAttribute(key);
    }

    /**
     * 删除Session值
     *
     * @param request
     * @param key
     */
    public static void removeAttr(HttpServletRequest request, String key) {
        request.getSession(true).removeAttribute(key);
    }

    /**
     * 设置用户信息 到session
     *
     * @param request
     * @param user
     */
    public static void setUser(HttpServletRequest request, User user) {
        request.getSession(true).setAttribute(SESSION_USER, user);
        request.getSession(true).setMaxInactiveInterval(MAX_TIME);
    }


    /**
     * 从session中获取用户信息
     *
     * @param request
     * @return SysUser
     */
    public static User getUser(HttpServletRequest request) {
        return (User) request.getSession(true).getAttribute(SESSION_USER);
    }


    /**
     * 从session中删除用户信息
     *
     * @param request
     * @return SysUser
     */
    public static void removeUser(HttpServletRequest request) {
        removeAttr(request, SESSION_USER);
    }

}