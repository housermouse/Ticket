package com.base.project.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.base.project.entity.Performance;
import com.base.project.entity.Ticket;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PerformanceService {

    @Value("WebConfig.damaiwang.ticket")
    String ticketUri4DMW;

    public JSONArray getArrForDMW(JSONObject jsonObject) {
        JSONArray dataArr = jsonObject.getJSONObject("pageData").getJSONArray("resultData");
        JSONArray newData = new JSONArray();
        if (null != dataArr&&dataArr.size()!=0) {
            dataArr.forEach(item -> {
                JSONObject obj = (JSONObject) item;
                Performance performance = new Performance();
                performance.setAddress(obj.getString("venue"));
                performance.setName(obj.getString("nameNoHtml"));
                performance.setPerformTime(obj.getString("showtime"));
                performance.setPicPath(obj.getString("verticalPic"));
                performance.setTicketValue(obj.getString("price"));
                performance.setTickets(getIicketInfo4DMW(obj.getString("projectid")));
                newData.add(performance);
            });
        }
        return newData;
    }

    public JSONObject getJSONData(JSONArray dataArr, String webName, String keyWorld) {
        JSONObject data = new JSONObject();
        data.put("webName", webName);
        data.put("keyWorld", keyWorld);
        data.put("data", dataArr);
        return data;
    }

    public ArrayList<Ticket> getIicketInfo4DMW(String id) {

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("itemId", id);
            jsonObject.put("apiVersion", "2.0");
            jsonObject.put("dmChannel", "pc@damai_pc");
            jsonObject.put("bizCode", "ali.china.damai");
            jsonObject.put("scenario", "itemsku");
            JSONObject data = CrawlerUtils.getDataforUrl("https://detail.damai.cn/subpage", jsonObject);
            return CrawlerUtils.ticketForDmwSeachData(data);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<Ticket>();
        }
    }

    public JSONArray getPerformInfoForMTL(String keyWorld) {
        String URL = "https://www.moretickets.com/search/" + keyWorld;
        JSONArray result = new JSONArray();
        try {
            Document doc = Jsoup.connect(URL).get();
            Elements elements = doc.getElementsByClass("show-items");
            for (Element element : elements) {
                Performance performance = new Performance();
                performance.setTickets(getTicketInfoForMTL(element.attr("data-sashowoid")));
                performance.setName(element.attr("data-sashowname"));
                performance.setTicketValue(element.getElementsByClass("show-price").text());
                performance.setPicPath(element.getElementsByTag("img").attr("data-src"));
                performance.setAddress(element.getElementsByClass("show-addr").text());
                performance.setPerformTime(element.getElementsByClass("show-time").text());
                result.add(performance);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }


        return result;
    }


    public ArrayList<Ticket> getTicketInfoForMTL(String sessionId) {

        JSONObject jsonObject = new JSONObject();
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        ArrayList<Ticket> resultJson = new ArrayList<Ticket>();
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
                        String cityId = ((JSONObject) o).getString("cityOID");
                        JSONObject param = new JSONObject();
                        param.put("locationCityOID", cityId);
                        resultJson = CrawlerUtils.getArrForMTLData(uri, param);

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
