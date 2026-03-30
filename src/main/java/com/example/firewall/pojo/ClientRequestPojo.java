package com.example.firewall.pojo;

public class ClientRequestPojo {

    public Long actionTime;
    public String ip;
    public String uri;
    public String query;
    public String method;
    public String threat_type;
    public String action;

    public void copyClientRequestData(Long actionTime,String ip,String uri,String query,String method,String threat_type,String action){

        this.actionTime = actionTime;
        this.ip = ip;
        this.uri = uri;
        this.query = query;
        this.method = method;
        this.threat_type = threat_type;
        this.action = action;

    }
    
}
