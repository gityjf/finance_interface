package com.tenwa.config;

import java.util.ResourceBundle;

/**
 * @program: finance_interface
 * @author: yjf
 * @create: 2019-12-26 10:54
 **/
public class Configure {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("config");

    public static final String RSAPublicKey = bundle.getString("RSAPublicKey");

    public static final String RSAPrivateKey = bundle.getString("RSAPrivateKey");



}
