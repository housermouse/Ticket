package com.base.project.util;

import com.alibaba.fastjson.JSONObject;

public class JsonBackUtil {
    public static JSONObject success(JSONObject data){
        JSONObject response = new JSONObject();
        response.put("code","1");
        response.put("data",data);
        response.put("note","接口访问成功");
        return response;
    }

    public static JSONObject fail(JSONObject data){
        JSONObject response = new JSONObject();
        response.put("code","-1");
        response.put("data",data);
        response.put("note","接口访问失败！");
        return response;
    }

}
