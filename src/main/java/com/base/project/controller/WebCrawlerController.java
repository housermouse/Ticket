package com.base.project.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.base.project.entity.User;
import com.base.project.service.PerformanceService;
import com.base.project.service.UserService;
import com.base.project.util.CrawlerUtils;
import com.base.project.util.JsonBackUtil;
import com.base.project.util.SessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RestController
@RequestMapping("/crawler")
public class WebCrawlerController {

    @Value("${WebConfig.damaiwang.index}")
    private String dmwIndex;

    @Value("${WebConfig.damaiwang.search}")
    private String dmwSearch;
    @Value("${WebConfig.damaiwang.SearchList}")
    private String SearchList;
    @Value("${WebConfig.damaiwang.SearchLikeList}")
    private String SearchLikeList;

    @Autowired
    PerformanceService performanceService;

    //首页初始化数据使用大麦网首页数据初始化
    @RequestMapping("/index")
    public JSONObject index(HttpServletRequest request, HttpServletResponse response){
        JSONObject respJson = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject params = new JSONObject();
        params.put("cat","1");
        params.put("destCity","全国");
        jsonArray.add(performanceService.getData(CrawlerUtils.getDataforUrl(dmwIndex,params)));
        params.put("cat","3");
        params.put("destCity","全国");
        jsonArray.add(performanceService.getData(CrawlerUtils.getDataforUrl(dmwIndex,params)));
        params.put("cat","6");
        params.put("destCity","全国");
        jsonArray.add(performanceService.getData(CrawlerUtils.getDataforUrl(dmwIndex,params)));
        params.put("cat","100");
        params.put("destCity","全国");
        jsonArray.add(performanceService.getData(CrawlerUtils.getDataforUrl(dmwIndex,params)));
        respJson.put("contentList",jsonArray);
        return JsonBackUtil.success(respJson);
    }

    @RequestMapping("/search")
    public JSONObject search(@RequestParam String keyword){
        JSONObject respJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("keyword",keyword);
        JSONArray searchData = new JSONArray();
        if(StringUtils.isBlank(keyword)){
            searchData.add(performanceService.getJSONData(new JSONArray(),"大麦网","DMW"));
            searchData.add(performanceService.getJSONData(new JSONArray(),"摩天轮","MTL"));
        }else {
            JSONArray dmwJsonArr = performanceService.getArrForDMW(CrawlerUtils.getDataforUrl(dmwSearch,params));
            searchData.add(performanceService.getJSONData(dmwJsonArr,"大麦网","DMW"));
            searchData.add(performanceService.getJSONData(performanceService.getPerformInfoForMTL(keyword),"摩天轮","MTL"));
        }
        respJson.put("searchData",searchData);
        return JsonBackUtil.success(respJson);
    }

    @RequestMapping("getSearchList")
    public JSONObject getSearchList(@RequestParam String keyword){
        JSONObject jsonObject = new JSONObject();
        String keyWorld = StringUtils.isBlank(keyword)?"":keyword;
        JSONObject params = new JSONObject();
        params.put("keyword",keyWorld);
        params.put("destCity","全国");
        JSONArray temp =CrawlerUtils.getDataforUrl(SearchList,params).getJSONArray("data");
        JSONArray result = new JSONArray();
        for(int i = 0 ;i<temp.size();i++){
            String tempStr = temp.getJSONObject(i).getString("name");
            String note = tempStr.replaceAll("<(/?\\S+)\\s*?[^<]*?(/?)>","");
             note = tempStr.replaceAll("&lt;span class=&quot;c4&quot;&gt;","");
             note = note.replaceAll("&lt;/span&gt;","");
            result.add(note);
        }

        jsonObject.put("list",result);
        return JsonBackUtil.success(jsonObject);
    }

    @RequestMapping("getCarousel")
    public JSONObject getCarousel(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("imgList",performanceService.getCarousel());
        return JsonBackUtil.success(jsonObject);
    }


    @RequestMapping("getHotSinger")
    public JSONObject getHotSinger(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hotSearch",performanceService.getHotSinger());
        return JsonBackUtil.success(jsonObject);
    }


    @RequestMapping("getLikeList")
    public JSONObject getLikeList(@RequestParam String keyword){
        JSONObject jsonObject = new JSONObject();
        String keyWorld = StringUtils.isBlank(keyword)?"":keyword;
        JSONObject params = new JSONObject();
        params.put("keyword",keyWorld);
        String  ids = CrawlerUtils.getDataforUrl(dmwSearch,params).getString("ids");
        params.put("projects",ids);
        JSONObject getData= CrawlerUtils.getDataforUrl(SearchLikeList,params);
        System.out.println(getData);
        JSONArray tempArray = getData.getJSONArray("suggest");
        JSONArray resultArray = new JSONArray();
        for(int i=0;i<tempArray.size();i++){
            JSONObject obj = tempArray.getJSONObject(i);
            JSONObject resultJson = new JSONObject();
            resultJson.put("type","ych");
            resultJson.put("img",obj.getString("verticalPic"));
            resultJson.put("name",obj.getString("projectName"));
            resultJson.put("address",obj.getString("venue"));
            resultJson.put("date",obj.getString("showTime"));
            resultJson.put("price",obj.getString("price"));
            resultJson.put("id",obj.getString("projectId"));
            resultArray.add(resultJson);
        }
        jsonObject.put("likeList",resultArray);
        return JsonBackUtil.success(jsonObject);
    }






}
