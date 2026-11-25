package com.example.cassandralog.entity;

import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("firewall_logs")
public class CassandraLogEntity {

    @PrimaryKey
    public UUID id;

    @Column("action_time")
    public Long actionTime;

    @Column("ip")
    public String ip;

    @Column("uri")
    public String uri;

    @Column("query")
    public String query;

    @Column("method")
    public String method;

    @Column("threat_type")
    public String threat_type;

    @Column("action")
    public String action;

    @Column("status")
    public String status;

    public CassandraLogEntity() {
    }

    public CassandraLogEntity(UUID id, Long actionTime, String ip, String uri, String query, String method, String threat_type, String action, String status) {
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
