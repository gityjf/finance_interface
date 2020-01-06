package com.tenwa.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @program:
 * @author: yjf
 * @create: 2019-12-25 18:21
 **/
public class XmlUtil {

    private static Map<String, Integer> countMap = new HashMap<>();
    private static final String prefix_xml = "<?xml version=\"1.0\" encoding = \"UTF-8\"?>";


    public static synchronized List<Map<String, String>> xmlToMap(Object param) throws Exception {
        List<Map<String, String>> results = new ArrayList<>();
        InputStream stream = null;
        if (param instanceof String) {
            stream = new ByteArrayInputStream(((String) param).getBytes(Charset.defaultCharset()));
        } else if (param instanceof byte[]) {
            stream = new ByteArrayInputStream((byte[]) param);
        }
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(stream);
        doc.getDocumentElement().normalize();
        NodeList nodeList = doc.getDocumentElement().getChildNodes();
        analysisNode(nodeList, results);
        try {
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    //递归解析xml节点
    private static void analysisNode(NodeList nodeList, List<Map<String, String>> nodes) {
        Map<String, String> innerMap = new LinkedHashMap<>();
        for (int idx = 0; idx < nodeList.getLength(); ++idx) {
            Node nodeItem = nodeList.item(idx);
            Element element = (Element) nodeItem;
            if (nodeItem.getNodeType() == 1) {
                int itemLength = nodeItem.getChildNodes().getLength();
                if (itemLength > 1) {
                    analysisNode(nodeItem.getChildNodes(), nodes);
                } else {
                    innerMap.put(element.getTagName(), element.getTextContent());
                }
            }
        }
        if (innerMap.size() > 0) {
            nodes.add(innerMap);
        }
    }

    /**
     * 支付指令xml生成样例
     *
     * @param payLists
     * @return
     */
    public static String payListToXml(List<Map<String, String>> payLists) {
        //发送时间
        String sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date());
        StringBuffer bf = new StringBuffer(prefix_xml);
        bf.append("<Iss_Itreasury><InstrReq>").append("<OperationType>1</OperationType>")
                .append("<SystemID>SAP</SystemID>").append("<BatchNo></BatchNo>").append("<IsManual>1</IsManual>")
                .append("<SendTime>" + sendTime + "</SendTime>")
                .append(standardCreXml("InstrContent", payLists))
                .append("</InstrReq></Iss_Itreasury>");
        return bf.toString();
    }


    /**
     * 查询支付指令xml生成样例
     *
     * @param queryLists
     * @return
     */
    public static String queryPayListToXml(List<Map<String, String>> queryLists) {
        String sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date());
        String SystemID = "SAP" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + String.format("%06d", 1);
        StringBuffer bf = new StringBuffer(prefix_xml);
        bf.append("<Iss_Itreasury><QueryReq>").append("<OperationType>2</OperationType>")
                .append("<SystemID>SAP</SystemID>").append("<SendTime>" + sendTime + "</SendTime>")
                .append(standardCreXml("QueryContent", queryLists))
                .append("</QueryReq></Iss_Itreasury>");
        return bf.toString();
    }


    /**
     * 查询行名行号xml生成样例
     *
     * @param lists
     * @return
     */
    public static String bankNoListToXml(List<Map<String, String>> lists) {
        String sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date());
        StringBuffer bf = new StringBuffer(prefix_xml);
        bf.append("<Iss_Itreasury><QueryReq>").append("<OperationType>12</OperationType>")
                .append("<SystemID>SAP</SystemID>").append("<SendTime>" + sendTime + "</SendTime>")
                .append(standardCreXml("QueryContent", lists))
                .append("</QueryReq></Iss_Itreasury>");
        return bf.toString();
    }

    /**
     * 通用xml报文体生成
     *
     * @param label  标签名
     * @param labels 内部标签及数据
     * @return
     */
    public static String standardCreXml(String label, List<Map<String, String>> labels) {
        StringBuffer bf = new StringBuffer();
        labels.forEach((map) -> {
            bf.append("<" + label + ">");
            map.forEach((key, value) -> {
                bf.append("<").append(key).append(">").append(value).append("</").append(key).append(">");
            });
            bf.append("</" + label + ">");
        });
        return bf.toString();
    }


    public static void main(String[] args) throws Exception {
//        String xml = payListToXml(psyInstructList());
//        System.out.println(xml);
//        List<Map<String, String>> maps = xmlToMap(xml.getBytes(Charset.defaultCharset()));
        List<Map<String, String>> maps = xmlToMap("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Iss_Itreasury><QueryRen><OperationType>12</OperationType><ProcessCode>0000</ProcessCode><ProcessDesc>查询账户交易流水接口调用成功</ProcessDesc><TotalCount>1</TotalCount><CurCount>1</CurCount><QueryNextPage></QueryNextPage><QueryContent><QueryBankCode/><QueryPageNumber/><REVERSE1/><REVERSE2/></QueryContent><RenContent><BankCode>001100011002</BankCode><BankName>中国人民银行营业管理部营业室</BankName><AreaProvince>北京</AreaProvince><AreaName>北京</AreaName><REVERSE1></REVERSE1><REVERSE2></REVERSE2></RenContent></QueryRen></Iss_Itreasury>");
        System.out.println(maps.size());
        for (Map<String, String> map : maps) {
            System.out.println("========================");
            map.forEach((key, value) -> {
                System.out.println(key.toUpperCase() + "-----" + value);
            });
        }
    }

    private static String testGenerateXml() {
        List<Map<String, String>> lists = new ArrayList<>();
        Map<String, String> map = new LinkedHashMap<>();
        map.put("QueryBankCode", "");
        map.put("QueryPageNumber", "10");
        map.put("REVERSE1", "18");
        map.put("REVERSE2", "你好");
        lists.add(map);
        String reStr = bankNoListToXml(lists);
        System.out.println(reStr);
        return reStr;
    }


    //测试支付指令
    private static List<Map<String, String>> psyInstructList() {
        //业务申请编号
        String ApplyCode = "SAP" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + String.format("%06d", 1);
        List<Map<String, String>> lists = new ArrayList<>();
        Map<String, String> map = new LinkedHashMap<>();
        map.put("ApplyCode", ApplyCode);
        map.put("CurrencyCode", "");
        map.put("ClientCode", "");
        map.put("ExpectDate", "2020-01-07");
        map.put("Amount", "1.00");
        map.put("PayType", "2");
        map.put("IsPrivatePayID", "0");
        map.put("PayAccountNo", "81010007000000000276");
        map.put("RecAccountNo", "01011560008001 ");
        map.put("RecAccountName", "物产中大财务公司1");
        map.put("RecBankName", "");
        map.put("RecBankProvince", "");
        map.put("RecBankCity", "");
        map.put("RecBankCNAPSNO", "");
        map.put("RecBankUnionNO", "");
        map.put("RecBankAgencyNO", "");
        map.put("PayAbstract", "测试接口");
        map.put("IsNonProductionID", "0");
        map.put("PlanProjectCode", "");
        map.put("CreateUserName", "001");
        map.put("UserPassword", MD5Util.stringToMD5("123456a"));
        map.put("SginText", "");
        map.put("MoneyUseCode", "");
        map.put("MoneyUseExplain", "");
        map.put("REVERSE1", "test1");
        map.put("REVERSE2", "");
        map.put("REVERSE3", "");
        map.put("REVERSE4", "");
        map.put("REVERSE5", "");
        lists.add(map);
        return lists;
    }



//    // 查询行号行名
//    String body = "<?xml version=\"1.0\" encoding = \"UTF-8\"?><Iss_Itreasury><QueryReq><OperationType>12</OperationType><SystemID>SAP</SystemID><SendTime>2020-01-06 10:39:11 560</SendTime><QueryContent><QueryBankCode></QueryBankCode><QueryPageNumber>10</QueryPageNumber><REVERSE1>18</REVERSE1><REVERSE2></REVERSE2></QueryContent></QueryReq></Iss_Itreasury>";
//    // 查询支付指令
//    String body =
//// 生成支付指令
//            String body =
//
//
//
//            HttpUtil.PostMsg("http://10.112.50.31:8080/FrontEnd/FrontEndServlet",body);
}
