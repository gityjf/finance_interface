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

    public static final String prefix_xml = "<?xml version=\"1.0\" encoding = \"UTF-8\"?>";


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
     * 创建支付指令xml生成
     *
     * @param payLists
     * @return
     */
    public static String createPayListToXml(List<Map<String, String>> payLists, String sendTime) {
        StringBuffer bf = new StringBuffer(prefix_xml);
        bf.append("<Iss_Itreasury><InstrReq>").append("<OperationType>1</OperationType>")
                .append("<SystemID>RDP</SystemID>").append("<BatchNo></BatchNo>").append("<IsManual>1</IsManual>")
                .append("<SendTime>" + sendTime + "</SendTime>")
                .append(standardCreXml("InstrContent", payLists))
                .append("</InstrReq></Iss_Itreasury>");
        return bf.toString();
    }


    /**
     * 查询支付指令xml生成
     *
     * @param queryLists
     * @return
     */
    public static String queryPayListToXml(List<Map<String, String>> queryLists) {
        String sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date());
        StringBuffer bf = new StringBuffer(prefix_xml);
        bf.append("<Iss_Itreasury><QueryReq>").append("<OperationType>2</OperationType>")
                .append("<SystemID>RDP</SystemID>").append("<SendTime>" + sendTime + "</SendTime>")
                .append(standardCreXml("QueryContent", queryLists))
                .append("</QueryReq></Iss_Itreasury>");
        return bf.toString();
    }


    /**
     * 通用xml报文体生成
     *
     * @param label    标签名
     * @param contents 内部标签及数据
     * @return
     */
    public static String standardCreXml(String label, List<Map<String, String>> contents) {
        StringBuffer bf = new StringBuffer();
        contents.forEach((map) -> {
            bf.append("<" + label + ">");
            map.forEach((key, value) -> {
                bf.append("<").append(key).append(">").append(value).append("</").append(key).append(">");
            });
            bf.append("</" + label + ">");
        });
        return bf.toString();
    }


    //测试支付指令
    public static List<Map<String, String>> testPayInstructList(int count) {
        //业务申请编号
        List<Map<String, String>> lists = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String ApplyCode = "RDP" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + String.format("%06d", i);
            Map<String, String> map = new LinkedHashMap<>();
            map.put("ApplyCode", ApplyCode);
            map.put("CurrencyCode", "");
            map.put("ClientCode", "");
            map.put("ExpectDate", "2020-01-07");
            map.put("Amount", "1.00");
            map.put("PayType", "1");
            map.put("IsPrivatePayID", "0");
            map.put("PayAccountNo", "81010007000000000531");
            map.put("RecAccountNo", "6893465989876589798875");
            map.put("RecAccountName", "物产中大财务公司1");

            Map<String, String> bankMap = BankNoUtil.queryBankInfoByBankNo("001100011002");
            map.put("RecBankName", bankMap.get("BankName"));
            map.put("RecBankProvince", bankMap.get("AreaProvince"));
            map.put("RecBankCity", bankMap.get("AreaName"));

            map.put("RecBankCNAPSNO", "12345678");

            map.put("RecBankUnionNO", "");
            map.put("RecBankAgencyNO", "");
            map.put("PayAbstract", "测试接口");
            map.put("IsNonProductionID", "0");
            map.put("PlanProjectCode", "");
            map.put("CreateUserName", "1900003");
            map.put("UserPassword", MD5Util.stringToMD5("123456"));
            map.put("SginText", "");
            map.put("MoneyUseCode", "");
            map.put("MoneyUseExplain", "");
            map.put("REVERSE1", "" + i);
            map.put("REVERSE2", "");
            map.put("REVERSE3", "");
            map.put("REVERSE4", "");
            map.put("REVERSE5", "");
            lists.add(map);
        }
        return lists;
    }


    //测试查询支付指令
    public static List<Map<String, String>> testQueryPayInstructList() {
        List<Map<String, String>> lists = new ArrayList<>();
        Map<String, String> map = new LinkedHashMap<>();
        map.put("APPLYCODE", "RZDP20200108132528273000001");
        map.put("REVERSE1", "19000030");
        map.put("REVERSE2", MD5Util.stringToMD5("123456"));
        map.put("REVERSE3", "");
        map.put("REVERSE4", "");
        map.put("REVERSE5", "");
        lists.add(map);
        return lists;
    }
}
