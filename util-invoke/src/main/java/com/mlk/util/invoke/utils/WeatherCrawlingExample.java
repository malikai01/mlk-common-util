package com.mlk.util.invoke.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 趴取网页上的今天的天气
 *
 * @author malikai
 * @date 2021年06月24日 14:48
 */
public class WeatherCrawlingExample {

    /**
     * 发起http get请求获取网页源代码
     *
     * @param requestUrl String    请求地址
     * @return String    该地址返回的html字符串
     */
    private static String httpRequest(String requestUrl) {

        StringBuffer buffer = null;
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        InputStream inputStream = null;
        HttpURLConnection httpUrlConn = null;

        try {
            // 建立get请求
            URL url = new URL(requestUrl);
            httpUrlConn = (HttpURLConnection) url.openConnection();
            httpUrlConn.setDoInput(true);
            httpUrlConn.setRequestMethod("GET");

            // 获取输入流
            inputStream = httpUrlConn.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            bufferedReader = new BufferedReader(inputStreamReader);

            // 从输入流读取结果
            buffer = new StringBuffer();
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (httpUrlConn != null) {
                httpUrlConn.disconnect();
            }
        }
        return buffer.toString();
    }

    /**
     * 过滤掉html字符串中无用的信息
     *
     * @param html String    html字符串
     * @return String    有用的数据
     */
    private static String htmlFiter(String html) {

        StringBuffer buffer = new StringBuffer();
        String str1 = "";
        buffer.append("");

        // 取出有用的范围
        Pattern p = Pattern.compile("(.*)(<li class=\"sky skyid lv1 on\">)(.*?)(</li>)(.*)");
        Matcher m = p.matcher(html);
        if (m.matches()) {
            str1 = m.group(3);
            // 匹配日期，注：日期被包含在<h2> 和 </h2>中
            p = Pattern.compile("(.*)(<h1>)(.*?)(</h1>)(.*)");
            m = p.matcher(str1);
            if (m.matches()) {
                buffer.append(m.group(3));
                buffer.append("\n天气：");
            }
            // 匹配天气，注：天气被包含在<p class="wea" title="..."> 和 </p>中
            p = Pattern.compile("(.*)(<p title=)(.*?)(class=\"wea\">)(.*?)(</p>)(.*)");
            m = p.matcher(str1);
            if (m.matches()) {
                buffer.append(m.group(5));
                buffer.append("\n温度：");
            }
            // 匹配温度，注：温度被包含在<p class=\"tem\"> <span> 和 </span><i>中
            p = Pattern.compile("(.*)(<p class=\"tem\"><span>)(.*?)(</span>)(/)(<i>)(.*?)(</i>)(.*)");
            m = p.matcher(str1);
            if (m.matches()) {
                buffer.append(m.group(3));
                buffer.append(m.group(5));
                buffer.append(m.group(7));
                buffer.append("\n风力：");
            }
            // 匹配风，注：<i> 和 </i> 中
            p = Pattern.compile("(.*)(<i>)(.*?)(</i>)(.*)");
            m = p.matcher(str1);
            if (m.matches()) {
                buffer.append(m.group(3));
            }
        }
        return buffer.toString();
    }

    /**
     * 对以上两个方法进行封装。
     *
     * @return
     */
    public static String getTodayTemperatureInfo() {
        // 调用第一个方法，获取html字符串
        String html = httpRequest("http://www.weather.com.cn/html/weather/101020100.shtml");
        // 调用第二个方法，过滤掉无用的信息
        return htmlFiter(html);
    }

    /**
     * 测试
     *
     * @param args
     */
    public static void main(String[] args) {
        String info = getTodayTemperatureInfo();
        System.out.println(info);
    }
}


