package com.tenwa.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.*;

public class Demo {
    private static final String SEND_URL = "http://10.112.50.31:8080/FrontEnd/FrontEndServlet";


    public static void main(String[] args) throws Exception {
        String flowId = "";
        //限制重复发起支付指令
        try {
            //查询所有待支付指令数据
            List<Map<String, String>> maps = queryPayInstrctionList();
            System.out.println(maps.size());
            int number = 1;
            //实际发送支付指令数据
            List<Map<String, String>> sendMaps = new ArrayList<Map<String, String>>();
            for (int i = 0; i < maps.size(); i++) {
                String planId = maps.get(i).get("REVERSE5");
                if (isNotLaunchPayInstruction(flowId, planId)) {
                    sendMaps.add(maps.get(i));
                    number++;
                    if (number == 100) {
                        //发送时间
                        String sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date());
                        savePayRecord(sendMaps,flowId,sendTime);
                        String payXml = XmlUtil.payListToXml(maps,sendTime);
                        System.out.println(payXml);
                        //调用前置机，发送支付指令
                        String receive = HttpUtil.post(SEND_URL, payXml);
                        number = 0;
                        sendMaps.clear();
                        //解析返回结果
                        List<Map<String, String>> recMaps = XmlUtil.xmlToMap(receive);
                        //保存生成支付记录
                    }
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean isNotLaunchPayInstruction(String flowId, String planId) throws Exception {
        List<Map<String, String>> dataBySql = OperateMapUtil.getDataBySql("select count(1) as num from PAY_INSTRUCTION where FLOWUNID = ? and PLAN_ID = ? and INSTRUCTIONSTATUSCOD  <> 1", flowId, planId);
        return Integer.valueOf(dataBySql.get(0).get("NUM")) == 0;
    }

    public static List<Map<String, String>> queryPayInstrctionList() {
        try {
            String querySql = "select 'RZDP'||to_char(systimestamp,'yyyymmddHHmissff')||trim(TO_CHAR(ROWNUM,'000000')) as \"ApplyCode\", 'nofill' as \"CurrencyCode\",'nofill' as \"ClientCode\",to_char(to_date(FACT_DATE,'yyyy-MM-dd'),'yyyy-MM-dd') as \"ExpectDate\" ,\n" +
                    "       FACT_MONEY as \"Amount\",'1' as \"PayType\",'0' as \"IsPrivatePayID\",ACC_NUMBER as \"PayAccountNo\",CLIENT_ACCNUMBER as \"RecAccountNo\", CLIENT_ACCOUNT as \"RecAccountName\",\n" +
                    "        CLIENT_BANK as \"RecBankName\",'省' as \"RecBankProvince\",'市' as \"RecBankCity\", 'NAPSNO' as \"RecBankCNAPSNO\", unionnumber as \"RecBankUnionNO\",'nofill' as \"RecBankAgencyNO\",\n" +
                    "       NVL(MEMO,'') as \"PayAbstract\" , '0' as \"IsNonProductionID\" ,'nofill' as \"PlanProjectCode\" ,'001' as \"CreateUserName\" ,'123456a' as \"UserPassword\" ,'nofill' as \"SginText\",\n" +
                    "       'nofill' as \"MoneyUseCode\" ,'nofill' as \"MoneyUseExplain\", 'nofill' as REVERSE1,'nofill' as REVERSE2 ,'nofill' as REVERSE3 , 'nofill' as REVERSE4 ,'nofill' as REVERSE5\n" +
                    "        from LC_FUND_INCOME_TEMP where unionnumber is not null and rownum < 5";
            List<Map<String, String>> maps = OperateMapUtil.getDataBySql(querySql);
//            for (Map<String, String> map1 : maps) {
//                System.out.println("========================");
//                map1.forEach((key, value) -> {
//                    System.out.println(key + "-----" + value);
//                });
//            }
            return maps;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void savePayRecord(List<Map<String, String>> recMaps, String flowId, String sendTime) {
        Connection conn = null;
        try {
            conn = JDBCUtil.getConn();
            String insertSql = "insert into pay_instruction (ID,PLAN_ID,FLOWUNID,OPERATIONTYPE,SYSTEMID,BATCHNO,ISMANUAL,SENDTIME,APPLYCODE,CURRENCYCODE,CLIENTCODE,EXPECTDATE,AMOUNT,PAYTYPE,ISPRIVATEPAYID,PAYACCOUNTNO,RECACCOUNTNO,RECACCOUNTNAME,RECBANKNAME,RECBANKPROVINCE,RECBANKCITY,RECBANKCNAPSNO,RECBANKUNIONNO,RECBANKAGENCYNO,PAYABSTRACT,ISNONPRODUCTIONID,PLANPROJECTCODE,CREATEUSERNAME,USERPASSWORD,SGINTEXT,MONEYUSECODE,MONEYUSEEXPLAIN,REVERSE1,REVERSE2,REVERSE3,REVERSE4,REVERSE5,INPUTUSERID,INPUTTIME,) " +
                                "values (sys_guid(),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement pst = conn.prepareStatement(insertSql);
            for (int i = 0; i < recMaps.size(); i++) {
                Map<String, String> map = recMaps.get(i);
                Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
                int paramindex = 0;
                pst.setString(++paramindex, recMaps.get(0).get("REVERSE5"));
                pst.setString(++paramindex, flowId);
                pst.setString(++paramindex, "1");
                pst.setString(++paramindex, "RZDP");
                pst.setString(++paramindex, "");
                pst.setString(++paramindex, "1");
                pst.setString(++paramindex, sendTime);
                while (it.hasNext()) {
                    Map.Entry<String, String> next = it.next();
                    pst.setString(++paramindex, next.getValue());
                }
                pst.setString(++paramindex,"1234");
                pst.setString(++paramindex,"to_char(sysdate,'yyyy-MM-dd hh24:mi:ss')");
                pst.addBatch();
            }
            pst.executeBatch();
            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try {
                    JDBCUtil.rollback();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }


    }


}
