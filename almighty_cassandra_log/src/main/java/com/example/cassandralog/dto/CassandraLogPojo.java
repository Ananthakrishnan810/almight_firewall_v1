package com.example.cassandralog.dto;

import java.util.UUID;

public class CassandraLogPojo {

    public UUID id;
    public Long actionTime;
    public String ip;
    public String uri;
    public String query;
    public String method;
    public String threat_type;
    public String action;
    public String status;

    public CassandraLogPojo() {
    }

    public CassandraLogPojo(UUID id, Long actionTime, String ip, String uri, String query, String method, String threat_type, String action, String status) {
        this.id = id;
        this.actionTime = actionTime;
        this.ip = ip;
        this.uri = uri;
        this.query = query;
        this.method = method;
        this.threat_type = threat_type;
        this.action = action;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
