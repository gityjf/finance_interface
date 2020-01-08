package com.tenwa.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @program: finance_interface
 * @author: yjf
 * @create: 2020-01-06 16:38
 **/
public class JDBCUtil {

    private static final String URL = "";
    private static final String USER = "";
    private static final String PASSWORD = "";
    private static final ThreadLocal<Connection> threadConn = new ThreadLocal();

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static Connection createConn() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            throw new RuntimeException("获取Connection出现异常！", e);
        }
        return conn;
    }

    public static Connection getConn() throws Exception {
        Connection conn = threadConn.get();
        if (conn == null) {
            conn = createConn();
            conn.setAutoCommit(false);
            threadConn.set(conn);
        }
        return conn;
    }

    public static void commit() {
        Connection conn = threadConn.get();
        if (conn != null) {
            try {
                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void rollback() {
        Connection conn = threadConn.get();
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close() {
        Connection conn = threadConn.get();
        if (conn != null) {
            threadConn.remove();
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        try {
            Connection conn = getConn();
            PreparedStatement pst = conn.prepareStatement("select * from lc_first");
            System.out.println(conn);
            conn.commit();
            Connection conn1 = threadConn.get();
            System.out.println(conn1);
            PreparedStatement pst1 = conn1.prepareStatement("select * from lc_first");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
