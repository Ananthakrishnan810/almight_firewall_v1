package com.example.cassandralog.dto;

public class ClientRequestPojo {

    public Long actionTime;
    public String ip;
    public String uri;
    public String query;
    public String method;
    public String threat_type;
    public String action;

    public ClientRequestPojo() {
    }

    public ClientRequestPojo(Long actionTime, String ip, String uri, String query, String method, String threat_type, String action) {
        this.actionTime = actionTime;
        this.ip = ip;
        this.uri = uri;
        this.query = query;
        this.method = method;
        this.threat_type = threat_type;
        this.action = action;
    }

    public void copyClientRequestData(Long actionTime, String ip, String uri, String query, String method, String threat_type, String action) {
        this.actionTime = actionTime;
        this.ip = ip;
        this.uri = uri;
        this.query = query;
        this.method = method;
        this.threat_type = threat_type;
        this.action = action;
    }

    public Long getActionTime() {
        return actionTime;
    }

    public void setActionTime(Long actionTime) {
        this.actionTime = actionTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getThreat_type() {
        return threat_type;
    }

    public void setThreat_type(String threat_type) {
        this.threat_type = threat_type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "ClientRequestPojo{" +
                "actionTime=" + actionTime +
                ", ip='" + ip + '\'' +
                ", uri='" + uri + '\'' +
                ", query='" + query + '\'' +
                ", method='" + method + '\'' +
                ", threat_type='" + threat_type + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}
