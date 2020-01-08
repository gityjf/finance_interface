package com.tenwa.util;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @program: finance_interface
 * @author: yjf
 * @create: 2020-01-08 10:55
 **/
public class BankNoUtil {

    /**
     * 查询行名行号xml生成样例
     *
     * @param lists
     * @return
     */
    public static String bankNoListToXml(List<Map<String, String>> lists) {
        String sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date());
        StringBuffer bf = new StringBuffer(XmlUtil.prefix_xml);
        bf.append("<Iss_Itreasury><QueryReq>").append("<OperationType>12</OperationType>")
                .append("<SystemID>RZDP</SystemID>").append("<SendTime>" + sendTime + "</SendTime>")
                .append(XmlUtil.standardCreXml("QueryContent", lists))
                .append("</QueryReq></Iss_Itreasury>");
        return bf.toString();
    }


    //测试查询行名行号
    public static String queryBankNoXml() {
        List<Map<String, String>> lists = new ArrayList<>();
        Map<String, String> map = new LinkedHashMap<>();
        map.put("QueryBankCode", "");
        map.put("QueryPageNumber", "1");
        map.put("REVERSE1", "");
        map.put("REVERSE2", "");
        lists.add(map);
        String reStr = bankNoListToXml(lists);
        System.out.println(reStr);
        return reStr;
    }

}
