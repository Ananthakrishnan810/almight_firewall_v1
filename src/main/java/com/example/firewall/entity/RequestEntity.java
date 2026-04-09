package com.example.firewall.entity;

import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;

import jakarta.persistence.Table;

@Table(name = "client_request")
public class RequestEntity {

    @PrimaryKey
    private UUID id;

    private String action;

    private Long actionTine;

    private String ip;

    private String uri;

    private String query;

    private String method;

    private String threat_type;
    
}