package com.tenwa.servlet;

import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.events.Characters;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @program: finance_interface
 * @author: yjf
 * @create: 2019-12-26 08:55
 **/
@WebServlet("/FrontEnd/FrontEndServlet")
public class FrontEndServlet extends HttpServlet {

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        req.setCharacterEncoding("UTF-8");
        BufferedReader reader = req.getReader();
        String strLine , resultStr= "";
        while ((strLine = reader.readLine()) != null){
            resultStr+=strLine;
        }
        System.out.println(resultStr);
    }
}
