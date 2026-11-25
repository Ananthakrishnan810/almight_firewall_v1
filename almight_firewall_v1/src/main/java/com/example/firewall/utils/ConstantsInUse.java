package com.example.firewall.utils;

import java.io.File;

public class ConstantsInUse {

    public static String URL_USE = "http://localhost:7080/auth";
    public static String PYTHON_CONNECTION = "http://localhost:5000";
    public static String DETECT_API = "/detect";
    public static String ENCODEDQUERY = "id=1%20OR%201=1";
    public static String ENCODEDXSS = "%3Cscript%3Ealert('XSS')%3C%2Fscript%3E";
    public static String ENCODEDPATH = "..%2F..%2F..%2Fetc%2Fpasswd";
    public static String ALLOW_THE_REQUEST = "ALLOW";
    public static String BLOCK_THE_REQUEST = "BLOCK";
    public static String REQUEST_NOT_ASSIGNED = "NOT ASSIGNED";
    public static String ACTIVE_STATUS = "active";
    public static String INACTIVE_STATUS = "inactive";
    public static String SUCCESS_MESSAGE = "success";
    public static String DUPLICATE_MESSAGE = "duplicate data";
    public static String RULEFILEPATH = resolveRuleFilePath();

    public static String resolveRuleFilePath() {
        String envPath = System.getenv("THREAT_RULES_PATH");
        if (envPath != null && new File(envPath).exists()) {
            return envPath;
        }
        // When running from almight_firewall_v1/ directory, threat_detection is a sibling ../threat_detection
        File siblingPath = new File("../threat_detection/rules.xlsx");
        if (siblingPath.exists()) {
            return siblingPath.getPath();
        }
        // When running from root workspace directory
        File rootPath = new File("threat_detection/rules.xlsx");
        if (rootPath.exists()) {
            return rootPath.getPath();
        }
        return "../threat_detection/rules.xlsx";
    }
}
