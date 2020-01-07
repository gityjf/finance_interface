package com.tenwa.util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @program: finance_interface
 * @author: yjf
 * @create: 2020-01-06 16:38
 **/
public class JDBCUtil {

    private static final String URL = "jdbc:oracle:thin:@119.3.11.192:1521:orcl";
    private static final String USER = "rzdp";
    private static final String PASSWORD = "rzdp2019";
    private static final ThreadLocal<Connection> threadConn = new ThreadLocal();

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static Connection createConn() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static Connection getConn() throws Exception {
        Connection conn = threadConn.get();
        if (conn == null) {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false);
            threadConn.set(conn);
        }
        return conn;
    }

    public static void commit() throws Exception {
        Connection conn = getConn();
        if (conn != null) {
            conn.commit();
            threadConn.remove();
        }
    }

    public static void rollback() throws Exception {
        Connection conn = getConn();
        if (conn != null) {
            getConn().rollback();
            threadConn.remove();
        }
    }


    public static void main(String[] args) throws Exception {


        for (int i = 0; i < 3; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        System.out.println(Thread.currentThread().getName());
//                        Connection conn = getConn();
//                        Connection conn1 = getConn();
//                        System.out.println(conn+"==="+conn1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        System.out.println("------------");


//        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
//        String querySql = "select * from LC_FUND_INCOME_TEMP where id <> ?";
//        List<Map<String, String>> maps = OperateMapUtil.getDataBySql(querySql, "5b63a2556a374c8b8615d044ebefbcfd");
//        for (Map<String, String> map : maps) {
//            System.out.println("========================");
//            map.forEach((key, value) -> {
//                System.out.println(key + "-----" + value);
//            });
//        }
    }

}
