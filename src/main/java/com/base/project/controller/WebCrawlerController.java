package com.base.project.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.base.project.entity.Performance;
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
import javax.xml.bind.Element;

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
        jsonArray.add(performanceService.getData(CrawlerUtils.getDataforUrl(dmwIndex,params),"演唱会"));
        params.put("cat","3");
        params.put("destCity","全国");
        jsonArray.add(performanceService.getData(CrawlerUtils.getDataforUrl(dmwIndex,params),"话剧歌剧"));
        jsonArray.add(performanceService.getindexForMTL("展览休闲"));
        jsonArray.add(performanceService.getindexForMTL("音乐会"));
        respJson.put("contentList",jsonArray);
        return JsonBackUtil.success(respJson);
    }

    @RequestMapping("/search")
    public JSONObject search(@RequestParam String keyword){
        JSONObject respJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("keyword",keyword.replaceAll(" ",""));
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

    @RequestMapping("/getSearch")
    public JSONObject getSearch(@RequestParam String keyword){
        JSONObject respJson = new JSONObject();
        JSONObject params = new JSONObject();
        if(StringUtils.isBlank(keyword)){
            keyword = "";
        }
        params.put("keyword",keyword.replaceAll(" ",""));
        respJson.put("related",CrawlerUtils.getSearch(keyword,"0"));
        respJson.put("recommend",CrawlerUtils.getSearch(keyword,"1"));
        respJson.put("recent",CrawlerUtils.getSearch(keyword,"2"));
        respJson.put("newsList",CrawlerUtils.getSearch(keyword,"3"));
        return JsonBackUtil.success(respJson);
    }

    @RequestMapping("getSearchList")
    public JSONObject getSearchList(@RequestParam String keyword){
        JSONObject jsonObject = new JSONObject();
        String keyWorld = StringUtils.isBlank(keyword)?"":keyword;
        JSONObject params = new JSONObject();
        params.put("keyword",keyword.replaceAll(" ",""));
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
        params.put("keyword",keyword.replaceAll(" ",""));
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

    @RequestMapping("getPerformanceInfo")
    public JSONObject getPerformanceInfo(HttpServletRequest request, HttpServletResponse response){
        JSONObject jsonObject = new JSONObject();
        String id =  request.getParameter("id");;
        String type =  request.getParameter("type");;
        if(StringUtils.isBlank(id)|| StringUtils.isBlank(type)){
            jsonObject.put("reamrk","参数传入不正确");
            return JsonBackUtil.fail(jsonObject);
        }
        if("0".equals(type)){
            JSONObject data = performanceService.getPerformeInfo4DMW(id);
            jsonObject.put("performInfo",performanceService.getDMWdata(data,id));
        }else if(type.equals("1")){
            String coverSrc =  request.getParameter("coverSrc");;
            String addr =  request.getParameter("addr");;
            JSONObject data = CrawlerUtils.getperforMTL(id);
            data.put("addr",addr);
            data.put("coverSrc",coverSrc);
            jsonObject.put("performInfo",data);
        }

        return JsonBackUtil.success(jsonObject);
    }





}
