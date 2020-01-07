package com.tenwa.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @program: finance_interface
 * @author: yjf
 * @create: 2019-12-30 18:08
 **/
public class HttpUtil {

    //设置http超时
    private static final int timeout = 2 * 60 * 1000;

    private static RequestConfig httpConfig = RequestConfig.custom()
            .setSocketTimeout(timeout).setConnectTimeout(timeout)
            .setConnectionRequestTimeout(timeout)
            .setExpectContinueEnabled(false).build();

    //具体负责发送请求的方法
    public static String post(String url, String body) throws Exception {
        return post(url,body,Charset.defaultCharset(),null);
    }

    //具体负责发送请求的方法
    public static String post(String url, String body, final Charset charset, Map<String, String> httpHeaders) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpPost post = new HttpPost(url);
            if (httpHeaders != null) {
                for (String key : httpHeaders.keySet()) {
                    post.setHeader(key, httpHeaders.get(key));
                }
            }
            post.setConfig(httpConfig);
            post.setEntity(new StringEntity(body, charset));
            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity, charset) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
            };
            String responseBody = client.execute(post, responseHandler);
            return responseBody;
        } finally {
            client.close();
        }
    }


    public static void main(String[] args) throws Exception {

//        List<Map<String, String>> lists = new ArrayList<>();
//        Map<String, String> map = new LinkedHashMap<>();
//        map.put("QueryBankCode", "001121013058");
//        map.put("QueryPageNumber", "");
//        map.put("REVERSE1", "测试");
//        map.put("REVERSE2", "");
//        lists.add(map);


        //发送时间
        String sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date());
//        String param =  XmlUtil.payListToXml(XmlUtil.payInstructList(),sendTime);  // 生成支付指令
//        String param =  XmlUtil.testGenerateXml();   //查询行名行号
        String param =XmlUtil.queryPayListToXml(XmlUtil.queryPayInstructList());
        System.out.println("send xml: " + param);
        try {
            String receive = post("http://10.112.50.31:8080/FrontEnd/FrontEndServlet", param, Charset.defaultCharset(), null);
            System.out.println(receive);
            List<Map<String, String>> maps = XmlUtil.xmlToMap(receive);
            for (Map<String, String> map1 : maps) {
                System.out.println("========================");
                map1.forEach((key, value) -> {
                    System.out.println(key.toUpperCase() + "-----" + value);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
