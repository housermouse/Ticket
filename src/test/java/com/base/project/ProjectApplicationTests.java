package com.base.project;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.base.project.controller.WebCrawlerController;
import com.base.project.entity.Ticket;
import com.base.project.mapper.UserMapper;
import com.base.project.service.PerformanceService;
import com.base.project.service.UserService;
import com.base.project.util.CrawlerUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;

@SpringBootTest
class ProjectApplicationTests {

    @Autowired
    UserService userService;
    @Autowired
    UserMapper userMapper;

    @Test
    void contextLoads() {
        PerformanceService performanceService = new PerformanceService();
       System.out.println( performanceService.getPerformInfoForMTL("杨千嬅"));
    }




    @Test
    void DMDataForToClass(){
        PerformanceService performanceService = new PerformanceService();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("keyword","林俊杰");
        JSONObject data = CrawlerUtils.getDataforUrl("https://search.damai.cn/searchajax.html",jsonObject);
        System.out.println(performanceService.getArrForDMW(data));

    }

    @Test
    void getTicketInfo(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("itemId","609379635985");
        jsonObject.put("apiVersion","2.0");
        jsonObject.put("dmChannel","pc@damai_pc");
        jsonObject.put("bizCode","ali.china.damai");
        jsonObject.put("scenario","itemsku");

        JSONObject data = CrawlerUtils.getDataforUrl("https://detail.damai.cn/subpage",jsonObject);
        System.out.println( CrawlerUtils.ticketForDmwSeachData(data));
    }

    @Test
    void getInfoForDMW(){
        PerformanceService performanceService = new PerformanceService();
        JSONObject respJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("keyword","杨千嬅");
        JSONArray dmwJsonArr = performanceService.getArrForDMW(CrawlerUtils.getDataforUrl("https://search.damai.cn/searchajax.html",params));
        respJson.put("searchData",performanceService.getJSONData(dmwJsonArr,"大麦网","DMW"));
        System.out.println( respJson);
    }

}
