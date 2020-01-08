package com.tenwa.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Demo {

    private static final String FRONT_MACHINE_URL = "http://10.112.50.31:8080/FrontEnd/FrontEndServlet";


    public static void main(String[] args) throws Exception {
        String flowId = "FBO2020010600000085";
        String userId = "1234";
        //限制重复发起支付指令
        try {
            //查询所有待支付指令数据
            List<Map<String, String>> maps = queryPayInstrctionList();
            System.out.println(maps.size());
            int batchNum = 0;    //批量支付笔数,100笔发送一次
            boolean sendStatus = false;
            //实际发送支付指令数据
            List<Map<String, String>> sendMaps = new ArrayList<Map<String, String>>();
            for (int i = 0; i < maps.size(); i++) {
                String planId = maps.get(i).get("REVERSE5");
                if (isNotLaunchPayInstruction(flowId, planId)) {
                    sendMaps.add(maps.get(i));
                    batchNum++;
                    if (maps.size() < 100 && batchNum == maps.size()) {
                        sendStatus = true;
                    } else if (batchNum == 100) {
                        sendStatus = true;
                    } else if (maps.size() > 100 && i == maps.size() - 1) {
                        sendStatus = true;
                    }
                    if (sendStatus) {
                        //发送时间
                        String sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date());
                        //保存支付指令记录
                        boolean isSucc = savePayRecord(sendMaps, flowId, userId, sendTime);
                        if (isSucc) {
                            // 生成支付指令xml
                            String payXml = XmlUtil.payListToXml(sendMaps, sendTime);
                            // 调用前置机,发送支付指令
                            String receive = HttpUtil.post(FRONT_MACHINE_URL, payXml);
                            if(!"null".equals(receive)){
                                // 解析返回结果
                                List<Map<String, String>> recLists = XmlUtil.xmlToMap(receive);
                                // 接口调用成功
                                if ("0000".equals(recLists.get(recLists.size() - 1).get("ProcessCode"))) {
                                    for (int j = 0; j < recLists.size() - 1; j++) {
                                        updatePayInstruction(recLists.get(j), userId);
                                    }
                                }else{ // 接口调用失败
                                    String processCode = recLists.get(recLists.size() - 1).get("ProcessCode");
                                    String ProcessDesc = recLists.get(recLists.size() - 1).get("ProcessDesc");
                                    batchUpdateFailRecord(sendMaps,processCode,ProcessDesc);
                                }
                            }
                        }
                        batchNum = 0;
                        sendStatus = false;
                        sendMaps.clear();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断支付指令不存在
     *
     * @param flowId
     * @param planId
     * @return
     * @throws Exception
     */
    public static boolean isNotLaunchPayInstruction(String flowId, String planId) throws Exception {
        List<Map<String, String>> dataBySql = OperateMapUtil.getDataBySql("select count(1) as num from PAY_INSTRUCTION where FLOWUNID = ? and PLAN_ID = ? and BUSINESSCODE = '0000' ", flowId, planId);
        return Integer.valueOf(dataBySql.get(0).get("NUM")) == 0;
    }

    /**
     * 保存支付指令记录
     *
     * @param recMaps
     * @param flowId
     * @param userId
     * @param sendTime
     * @return
     */
    private static boolean savePayRecord(List<Map<String, String>> recMaps, String flowId, String userId, String sendTime) {
        boolean isSucc = false;
        PreparedStatement pst = null;
        try {
            Connection conn = JDBCUtil.getConn();
            String insertSql = "insert into PAY_INSTRUCTION (ID,PLAN_ID,FLOWUNID,OPERATIONTYPE,SYSTEMID,BATCHNO,ISMANUAL,SENDTIME,APPLYCODE,CURRENCYCODE,CLIENTCODE,EXPECTDATE,AMOUNT,PAYTYPE,ISPRIVATEPAYID,PAYACCOUNTNO,RECACCOUNTNO,RECACCOUNTNAME,RECBANKNAME,RECBANKPROVINCE,RECBANKCITY,RECBANKCNAPSNO,RECBANKUNIONNO,RECBANKAGENCYNO,PAYABSTRACT,ISNONPRODUCTIONID,PLANPROJECTCODE,CREATEUSERNAME,USERPASSWORD,SGINTEXT,MONEYUSECODE,MONEYUSEEXPLAIN,REVERSE1,REVERSE2,REVERSE3,REVERSE4,REVERSE5,INPUTUSERID,INPUTTIME) " +
                    " values (sys_guid(),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,to_char(sysdate,'yyyy-MM-dd hh24:mi:ss'))";
            pst = conn.prepareStatement(insertSql);
            for (int i = 0; i < recMaps.size(); i++) {
                Map<String, String> map = recMaps.get(i);
                Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
                int paramindex = 0;
                // 备用字段5记录 plan_id
                pst.setString(++paramindex, recMaps.get(0).get("REVERSE5"));
                pst.setString(++paramindex, flowId);
                pst.setString(++paramindex, "1");
                pst.setString(++paramindex, "RZDP");
                pst.setString(++paramindex, "");
                pst.setString(++paramindex, "1");
                pst.setString(++paramindex, sendTime);
                while (it.hasNext()) {
                    Map.Entry<String, String> next = it.next();
                    pst.setString(++paramindex, ("nofill".equals(next.getValue()) ? "" : next.getValue()));
                }
                pst.setString(++paramindex, userId);
                pst.addBatch();
            }
            pst.executeBatch();
            pst.clearBatch();
            JDBCUtil.commit();
            isSucc = true;

        } catch (Exception e) {
            e.printStackTrace();
            try {
                JDBCUtil.rollback();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                JDBCUtil.close();
                pst.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return isSucc;
        }
    }


    /**
     * 查询待生成支付指令数据(from DB)
     *
     * @return
     */
    public static List<Map<String, String>> queryPayInstrctionList() {
        List<Map<String, String>> maps = null;
        try {
            String querySql = "select 'RZDP'||to_char(systimestamp,'yyyymmddHHmissff')||trim(TO_CHAR(ROWNUM,'000000')) as \"ApplyCode\", 'nofill' as \"CurrencyCode\",'nofill' as \"ClientCode\",to_char(to_date(FACT_DATE,'yyyy-MM-dd'),'yyyy-MM-dd') as \"ExpectDate\" ,\n" +
                    "       FACT_MONEY as \"Amount\",'1' as \"PayType\",'0' as \"IsPrivatePayID\",ACC_batchNum as \"PayAccountNo\",CLIENT_ACCbatchNum as \"RecAccountNo\", CLIENT_ACCOUNT as \"RecAccountName\",\n" +
                    "        CLIENT_BANK as \"RecBankName\",'省' as \"RecBankProvince\",'市' as \"RecBankCity\", 'NAPSNO' as \"RecBankCNAPSNO\", unionbatchNum as \"RecBankUnionNO\",'nofill' as \"RecBankAgencyNO\",\n" +
                    "       NVL(MEMO,'') as \"PayAbstract\" , '0' as \"IsNonProductionID\" ,'nofill' as \"PlanProjectCode\" ,'001' as \"CreateUserName\" ,'123456a' as \"UserPassword\" ,'nofill' as \"SginText\",\n" +
                    "       'nofill' as \"MoneyUseCode\" ,'nofill' as \"MoneyUseExplain\", 'nofill' as REVERSE1,'nofill' as REVERSE2 ,'nofill' as REVERSE3 , 'nofill' as REVERSE4 ,PLAN_ID as REVERSE5\n" +
                    "        from LC_FUND_INCOME_TEMP where unionbatchNum is not null and rownum < 5";
            maps = OperateMapUtil.getDataBySql(querySql);
//            for (Map<String, String> map1 : maps) {
//                System.out.println("========================");
//                map1.forEach((key, value) -> {
//                    System.out.println(key + "-----" + value);
//                });
//            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return maps;
        }
    }

    /**
     * 批量更新调用接口失败状态
     * @param payInstructMaps
     * @param processCode
     * @param processDesc
     */
    public static void batchUpdateFailRecord(List<Map<String, String>> payInstructMaps,String processCode,String processDesc){
        PreparedStatement pst = null;
        try {
            Connection conn = JDBCUtil.getConn();
            String batchUpdateSql = "update PAY_INSTRUCTION set PROCESSCODE = ? ,PROCESSDESC= ? where APPLYCODE = ?";
            pst = conn.prepareStatement(batchUpdateSql);
            for (int i = 0; i < payInstructMaps.size(); i++) {
                Map<String, String> map = payInstructMaps.get(i);
                pst.setString(1,processCode);
                pst.setString(2,processDesc);
                pst.setString(3,map.get("ApplyCode"));
                pst.addBatch();
            }
            pst.executeBatch();
            pst.clearBatch();
            JDBCUtil.commit();
        } catch (Exception e){
            e.printStackTrace();
            JDBCUtil.rollback();
        }finally {
            JDBCUtil.close();
        }
    }
    /**
     * 逐条更新支付指令记录(调用财企接口返回结果)
     *
     * @param payInstructMap
     * @param userId
     */
    public static void updatePayInstruction(Map<String, String> payInstructMap, String userId) {
        Connection conn = null;
        PreparedStatement pst = null;
        try {
            conn = JDBCUtil.createConn();
            String updateSql = "update PAY_INSTRUCTION set PROCESSCODE = '0000',BUSINESSCODE = ?,BUSINESSDESC = ?,INSTRUCTIONSTATUSCOD =?,INSTRUCTIONSTATUSDESC = ?,UPDATEUSERID = ? ,UPDATETIME = to_char(sysdate,'yyyy-MM-dd hh24:mi:ss') where APPLYCODE = ?";
            pst = conn.prepareStatement(updateSql);
            pst.setString(1, payInstructMap.get("BusinessCode"));
            pst.setString(2, payInstructMap.get("BusinessDesc"));
            pst.setString(3, payInstructMap.get("InstructionStatusCode"));
            pst.setString(4, payInstructMap.get("InstructionStatusDesc"));
            pst.setString(5, userId);
            pst.setString(6, payInstructMap.get("ApplyCode"));
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 查询支付状态(调用财企接口)
     * @param flowId
     * @param userId
     */
    public static void queryPayInstructionStatus(String flowId, String userId) {
        try {
            String querySql = "select APPLYCODE as \"ApplyCode\" from PAY_INSTRUCTION where FLOWUNID = ? and  BUSINESSCODE = '0000' ;";
            List<Map<String, String>> applyCodeMaps = OperateMapUtil.getDataBySql(querySql, flowId);
            if(applyCodeMaps.size()>0){
                applyCodeMaps.forEach(map->{
                    map.forEach((key,value)->{
                        map.put("REVERSE1", "190000");
                        map.put("REVERSE2", MD5Util.stringToMD5("123456"));
                        map.put("REVERSE3", "");
                        map.put("REVERSE4", "");
                        map.put("REVERSE5", "");
                    });
                });
                int batchNum = 0;
                boolean sendStatus = false;
                //实际发送支付指令数据
                List<Map<String, String>> sendMaps = new ArrayList<Map<String, String>>();
                for (int i = 0; i < applyCodeMaps.size(); i++) {
                    sendMaps.add(applyCodeMaps.get(i));
                    batchNum++;
                    if (applyCodeMaps.size() < 100 && batchNum == applyCodeMaps.size()) {
                        sendStatus = true;
                    } else if (batchNum == 100) {
                        sendStatus = true;
                    } else if (applyCodeMaps.size() > 100 && i == applyCodeMaps.size() - 1) {
                        sendStatus = true;
                    }
                    if (sendStatus) {
                        //调用财企接口查询支付状态
                        String queryXml = XmlUtil.queryPayListToXml(sendMaps);
                        String receive = HttpUtil.post(FRONT_MACHINE_URL, queryXml);
                        if(!"null".equals(receive)){
                            // 解析返回结果
                            List<Map<String, String>> recLists = XmlUtil.xmlToMap(receive);
                            // 接口调用成功
                            if ("0000".equals(recLists.get(recLists.size() - 1).get("ProcessCode"))) {
                                for (int j = 0; j < recLists.size() - 1; j++) {
                                    updatePayInstruction(recLists.get(j), userId);
                                }
                            }else{ // 接口调用失败
                                String processCode = recLists.get(recLists.size() - 1).get("ProcessCode");
                                String ProcessDesc = recLists.get(recLists.size() - 1).get("ProcessDesc");
                                batchUpdateFailRecord(sendMaps,processCode,ProcessDesc);
                            }
                        }
                        batchNum = 0;
                        sendStatus = false;
                        sendMaps.clear();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
