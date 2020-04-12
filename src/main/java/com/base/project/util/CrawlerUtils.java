package com.base.project.util;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.base.project.entity.Ticket;
import com.base.project.service.PerformanceService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class CrawlerUtils {




    public static String paramToString(JSONObject parms) {
        StringBuilder sb = new StringBuilder();
        Iterator iter = parms.entrySet().iterator();
        while (iter.hasNext()) {
            JSONObject.Entry entry = (JSONObject.Entry) iter.next();
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue() != null ? entry.getValue() : "");
            sb.append("&");
        }
        return sb.toString();
    }

    public static ArrayList<Ticket> ticketForDmwSeachData(JSONObject Json) {
        try {
            ArrayList<Ticket> tickInfos = new ArrayList<Ticket>();
            JSONObject perform = Json.getJSONObject("perform");
            JSONArray tickArr = perform.getJSONArray("skuList");
            for (Object o : tickArr) {
                JSONObject obj = (JSONObject) o;
                Ticket ticket = new Ticket();
                ticket.setNumber(obj.getString("mq"));
                for (int i = 0; i < obj.getIntValue("mq"); i++) {
                    ticket.getPrices().add(obj.getString("dashPrice"));
                }
                ticket.setPriceName(obj.getString("priceName"));
                tickInfos.add(ticket);
            }
            return tickInfos;
        } catch (Exception e) {
            return new ArrayList<Ticket>();
        }
    }

    public static JSONObject getDataforUrl(String url, JSONObject parms) {
        JSONObject jsonObject = new JSONObject();
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        // 参数
        String sb = CrawlerUtils.paramToString(parms);
        // 创建Get请求
        HttpGet httpGet = new HttpGet(url + "?" + sb);

        httpGet.addHeader("referer","https://www.damai.cn/");
        // 响应模型
        CloseableHttpResponse response = null;
        try {
            // 配置信息
            RequestConfig requestConfig = RequestConfig.custom()
                    // 设置连接超时时间(单位毫秒)
                    .setConnectTimeout(5000)
                    // 设置请求超时时间(单位毫秒)
                    .setConnectionRequestTimeout(5000)
                    // socket读写超时时间(单位毫秒)
                    .setSocketTimeout(5000)
                    // 设置是否允许重定向(默认为true)
                    .setRedirectsEnabled(true).build();

            // 将上面的配置信息 运用到这个Get请求里
            httpGet.setConfig(requestConfig);

            // 由客户端执行(发送)Get请求
            response = httpClient.execute(httpGet);

            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();

            System.out.println("响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
                System.out.println("响应内容长度为:" + responseEntity.getContentLength());
                String result = EntityUtils.toString(responseEntity);

                int begin = result.indexOf("{");
                int end = result.lastIndexOf("}");
                if(result.startsWith("[") && result.endsWith("]")){
                    try{
                        jsonObject.put("data",JSONArray.parseArray(result));
                    }catch (Exception e){}

                }
                if (begin != -1 && end != -1) {
                    try{
                        result = result.substring(begin, end + 1);
                        jsonObject.putAll(JSONObject.parseObject(result));
                    }catch (JSONException e){

                    }

                }

            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }
    }

    static public ArrayList<Ticket> getArrForMTLData(String uri, JSONObject param) {
        ArrayList<Ticket> TicketArr = new ArrayList<Ticket>();
        try {
            JSONObject result = CrawlerUtils.getDataforUrl(uri, param);
            JSONArray data = result.getJSONObject("result").getJSONArray("data");

            for (Object datum : data) {
                JSONArray seatPlan = ((JSONObject) datum).getJSONArray("seatPlan");
                for (Object o : seatPlan) {
                    JSONObject obj = (JSONObject) o;
                    JSONArray tickets = obj.getJSONArray("tickets");
                    Ticket ticket = new Ticket();

                    ticket.setPriceName(obj.getString("comments") + obj.getString("originalPrice") + "元");
                    ticket.setNumber(tickets.size() + "");
                  try {
                      tickets.forEach(item -> {
                          ticket.getPrices().add(((JSONObject) item).getString("price"));
                      });
                  }catch (Exception e){

                  }
                    TicketArr.add(ticket);

                }
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
            return new ArrayList<Ticket>();
        }


        return TicketArr;
    }

    public static JSONArray getTIcketInfoForDMW(JSONObject perform,JSONArray jsonArray){

        JSONArray tickArr = perform.getJSONArray("skuList");

        for (Object o : tickArr) {
            JSONObject obj = (JSONObject) o;
            JSONObject ticket = new JSONObject();
            JSONArray tempArray = new JSONArray();
            JSONObject ticketInfo = new JSONObject();
            ticket.put("isNoTicket","0".equals(obj.getString("salableQuantity")));
            ticket.put("originPrice",obj.getString("dashPrice"));
            ticket.put("originName","大麦网");
            ticket.put("originPic","https://img.alicdn.com/tfs/TB1otMASmzqK1RjSZPxXXc4tVXa-167-60.png");
            ticketInfo.put("priceLevel",ticket.getString("originPrice"));
            tempArray.add(ticket);
            ticketInfo.put("origin",tempArray);
            jsonArray.add(ticketInfo);
        }

        return jsonArray;
    }

    public static JSONArray getSearch(String keyword,String order){
        PerformanceService performanceService = new PerformanceService();
        JSONObject respJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("keyword",keyword.replaceAll(" ",""));
        if(StringUtils.isBlank(order)){
            order = "";
        }
        params.put("order",order);
        JSONArray searchData = new JSONArray();
        JSONArray dmwJsonArr = performanceService.getArrForDmw(CrawlerUtils.getDataforUrl("https://search.damai.cn/searchajax.html", params));
        for (Object e:dmwJsonArr){
            JSONObject obj = (JSONObject)e;
            JSONObject result = new JSONObject();
            result.put("tag",obj.getString("categoryname"));
            result.put("img",obj.getString("verticalPic"));
            result.put("title",obj.getString("nameNoHtml"));
            result.put("singer",obj.getString("actors").replaceAll("<(/?\\S+)\\s*?[^<]*?(/?)>",""));
            result.put("addr",obj.getString("venue"));
            result.put("time",obj.getString("showtime"));
            result.put("price",obj.getString("price"));
            result.put("sell",obj.getString("showstatus"));
            result.put("id",obj.getString("projectid"));
            searchData.add(result);

        }
        return searchData;
    }

    public static JSONObject getperforMTL(String sessionId) {

        JSONObject jsonObject = new JSONObject();
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
       JSONObject resultJson = new JSONObject();
        String uri = "https://www.moretickets.com/showapi/pub/v1_2/show/" + sessionId + "/sessionone";
        // 创建Get请求
        HttpGet httpGet = new HttpGet(uri);
        // 响应模型
        CloseableHttpResponse response = null;
        try {
            // 配置信息
            RequestConfig requestConfig = RequestConfig.custom()
                    // 设置连接超时时间(单位毫秒)
                    .setConnectTimeout(5000)
                    // 设置请求超时时间(单位毫秒)
                    .setConnectionRequestTimeout(5000)
                    // socket读写超时时间(单位毫秒)
                    .setSocketTimeout(5000)
                    // 设置是否允许重定向(默认为true)
                    .setRedirectsEnabled(true).build();

            // 将上面的配置信息 运用到这个Get请求里
            httpGet.setConfig(requestConfig);

            // 由客户端执行(发送)Get请求
            response = httpClient.execute(httpGet);

            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();

            System.out.println("响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
                System.out.println("响应内容长度为:" + responseEntity.getContentLength());
                String result = EntityUtils.toString(responseEntity);
                int begin = result.indexOf("{");
                int end = result.lastIndexOf("}");
                if (begin != -1 && end != -1) {
                    result = result.substring(begin, end + 1);
                    jsonObject.putAll(JSONObject.parseObject(result));
                    JSONObject resultData = jsonObject.getJSONObject("result");
                    JSONArray data = resultData.getJSONArray("data");
                    for (Object o : data) {
                        JSONObject obj = (JSONObject) o;
                        JSONObject temp = new JSONObject();
                        temp.put("time",obj.getString("showTime"));
                        temp.put("title",obj.getJSONObject("showStatus").getString("displayName"));
                        resultJson.put("presell","temp");
                        resultJson.put("time",obj.getString("showTime"));
                        resultJson.put("title",obj.getString("showTime"));
                        resultJson.put("gotoURL","https://www.moretickets.com/content/"+sessionId);
                        JSONArray tickets = new JSONArray();
                        for(Object item :obj.getJSONArray("seatplans")){
                            JSONObject ticket = new JSONObject();
                            JSONObject object = new JSONObject();
                            ticket.put("originPic","https://www.moretickets.com/images/logo-b17998409b.png");
                            ticket.put("originName","摩天轮");
                            ticket.put("originPrice",((JSONObject) item).getString("minPrice"));
                            ticket.put("isNoTicket",((JSONObject) item).getString("limitation"));
                            JSONArray objects = new JSONArray();
                            objects.add(ticket);
                            object.put("origin",objects);
                            object.put("priceLevel",ticket.getString("originPrice"));
                            tickets.add(object);
                        }
                        resultJson.put("ticket",tickets);
                        break;
                    }
                }

            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return resultJson;
    }


}
