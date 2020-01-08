package com.tenwa.util;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestDemo {

    public static void main(String[] args) {
        try{
            int batchNum = 0;
            boolean sendStatus = false;

            List<Map<String, String>> maps = XmlUtil.payInstructList(101);
            List<Map<String, String>> sendMaps = new ArrayList<Map<String, String>>();

            for (int i = 0; i < maps.size(); i++) {
                sendMaps.add(maps.get(i));
                batchNum++;
                if (maps.size() < 100 && batchNum == maps.size()) {
                    System.out.println(sendMaps.size()+"....."+batchNum);
                    sendStatus = true;
                } else if (batchNum == 100) {
                    System.out.println(sendMaps.size()+"++++"+batchNum);
                    sendStatus = true;
                } else if (maps.size() > 100 && i==maps.size()-1) {
                    sendStatus = true;
                    System.out.println(sendMaps.size()+"----"+batchNum);
                }
                if (sendStatus) {
                    batchNum = 0;
                    sendStatus = false;
                    sendMaps.clear();
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }


}
