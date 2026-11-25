package com.example.firewall.utils;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class StringFormmer {

    public static void main(String[] args) {
        
        JSONObject obj = new JSONObject();

        obj.put("name", "John");
        obj.put("age", 25);

        System.out.println(obj.toString());

    }

    public String formString(long actionTime, String ip, String uri, String query, String method, String threat_type, String action){

        JSONObject obj = new JSONObject();

        obj.put("timeStamp", actionTime);
        obj.put("ip", ip);
        obj.put("uri", uri);
        obj.put("query", query);
        obj.put("method", method);
        obj.put("threat_type", threat_type);
        obj.put("action", action);

        return obj.toString();
    }
}