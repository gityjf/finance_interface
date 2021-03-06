package com.tenwa.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

/**
 * @program: finance_interface
 * @author: yjf
 * @create: 2020-01-06 17:02
 **/
public class OperateMapUtil {

    public static List<Map<String, String>> getDataBySql(String sql, Object... params) throws Exception {
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        Connection conn = JDBCUtil.createConn();
        PreparedStatement pst = conn.prepareStatement(sql);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                Object o  = params[i];
                if (o instanceof String) {
                    pst.setString(i + 1, (String) o);
                }
            }
        }
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            data.add(getASResultSetToMap(rs));
        }
        conn.close();
        return data;
    }

    private static Map<String, String> getASResultSetToMap(ResultSet rs) throws Exception {
        Map<String, String> paramMap = new LinkedHashMap<String, String>();
        ResultSetMetaData rd = rs.getMetaData();
        for (int i = 1; i <= rd.getColumnCount(); i++) {
            paramMap.put(rd.getColumnLabel(i), rs.getString(i));
        }
        return paramMap;
    }

}
