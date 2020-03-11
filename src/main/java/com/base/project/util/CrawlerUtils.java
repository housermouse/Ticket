package com.base.project.util;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.fastjson.JSONPObject;
import com.base.project.entity.Ticket;
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
                if (begin != -1 && end != -1) {
                    result = result.substring(begin, end + 1);
                    jsonObject.putAll(JSONObject.parseObject(result));
                }

            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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


        return jsonObject;
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


}
