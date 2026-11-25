package com.example.cassandralog.utils;

public class ConstantsInUse {

    public static final String TOPIC = "firewall_logs";
    public static final String KEYSPACE_NAME = "firewall_key";
    public static final String TABLE_NAME = "firewall_logs";

    public static final String ALLOW_THE_REQUEST = "ALLOW";
    public static final String BLOCK_THE_REQUEST = "BLOCK";
    public static final String REQUEST_NOT_ASSIGNED = "NOT ASSIGNED";

    public static final String ACTIVE_STATUS = "active";
    public static final String INACTIVE_STATUS = "inactive";

    public static final String SUCCESS_MESSAGE = "success";
    public static final String FAILURE_MESSAGE = "failure";
    public static final String DUPLICATE_MESSAGE = "duplicate data";
}
