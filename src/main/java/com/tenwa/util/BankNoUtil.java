package com.tenwa.util;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @program: finance_interface
 * @author: yjf
 * @create: 2020-01-08 10:55
 **/
public class BankNoUtil {

    public static void main(String[] args) throws Exception{
//        System.out.println(queryBankInfoByBankNo("001100011002"));
        batchImportBankInfo(4);
    }


    public static void batchImportBankInfo(int pageCount){
        try {
            Connection conn = JDBCUtil.getConn();
            String insertSql = "insert into t_bank_info values(?,?,?,?,to_char(sysdate,'yyyy-MM-dd hh24:mi:ss'))";
            PreparedStatement pst = conn.prepareStatement(insertSql);
            for (int i = 1; i < pageCount; i++) {
                List<Map<String, String>> lists = new ArrayList<>();
                Map<String, String> map = new LinkedHashMap<>();
                map.put("QUERYBANKCODE", "");
                map.put("QUERYPAGENUMBER", i+"");
                map.put("REVERSE1", "");
                map.put("REVERSE2", "");
                lists.add(map);
                String body = bankNoListToXml(lists);
                String receive = HttpUtil.post("http://10.112.50.31:8080/FrontEnd/FrontEndServlet", body);
                System.out.println(receive);
                if(!"null".equals(receive)){
                    List<Map<String, String>> maps = XmlUtil.xmlToMap(receive);
                    if("0000".equals(maps.get(maps.size()-1).get("ProcessCode"))){
                        for (int j = 1; j < maps.size()-1 ; j++) {
                            Map<String,String> fact = maps.get(j);
                            pst.setString(1,fact.get("BankCode"));
                            pst.setString(2,fact.get("BankName"));
                            pst.setString(3,fact.get("AreaProvince"));
                            pst.setString(4,fact.get("AreaName"));
                            pst.addBatch();
                        }
                        pst.executeBatch();
                        pst.clearBatch();
                        conn.commit();
                    }
                }
            }
        }catch (Exception e){
            JDBCUtil.rollback();
            e.printStackTrace();
        }finally {
            JDBCUtil.close();
        }
    }


    /**
     *  生成xml(查询行名行号)
     * @param lists
     * @return
     */
    public static String bankNoListToXml(List<Map<String, String>> lists) {
        String sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date());
        StringBuffer bf = new StringBuffer(XmlUtil.prefix_xml);
        bf.append("<Iss_Itreasury><QueryReq>").append("<OperationType>12</OperationType>")
                .append("<SystemID>RZDP</SystemID>").append("<SendTime>"+sendTime+"</SendTime>")
                .append(XmlUtil.standardCreXml("QueryContent", lists))
                .append("</QueryReq></Iss_Itreasury>");
        return bf.toString();
    }


    /**
     * 根据页码查询银行信息
     * @param pageNum
     * @return
     */
    public static String queryBankInfoXml(String pageNum) {
        String receiveXml = null;
        try {
            List<Map<String, String>> lists = new ArrayList<>();
            Map<String, String> map = new LinkedHashMap<>();
            map.put("QUERYBANKCODE", "");
            map.put("QUERYPAGENUMBER", pageNum);
            map.put("REVERSE1", "");
            map.put("REVERSE2", "");
            lists.add(map);
            String body = bankNoListToXml(lists);
            String receive = HttpUtil.post("http://10.112.50.31:8080/FrontEnd/FrontEndServlet", body);
            receiveXml= receive;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            return receiveXml;
        }
    }


    /**
     * 根据银行行号查询银行信息
     * @param bankNo
     * @return
     */
    public static Map<String,String> queryBankInfoByBankNo(String bankNo ){
        Map<String,String> recMap = null;
        try {
            List<Map<String, String>> lists = new ArrayList<>();
            Map<String, String> map = new LinkedHashMap<>();
            map.put("QUERYBANKCODE",bankNo);
            map.put("QUERYPAGENUMBER", "");
            map.put("REVERSE1", "");
            map.put("REVERSE2", "");
            lists.add(map);
            String body = bankNoListToXml(lists);
            String receive = HttpUtil.post("http://10.112.50.31:8080/FrontEnd/FrontEndServlet", body);
            System.out.println(receive);
            if(!"null".equals(receive)){
                List<Map<String, String>> analysisRetMap = XmlUtil.xmlToMap(receive);
                if("0000".equals(analysisRetMap.get(analysisRetMap.size()-1).get("ProcessCode"))){
                    recMap =  analysisRetMap.get(1);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return recMap;
    }



}
