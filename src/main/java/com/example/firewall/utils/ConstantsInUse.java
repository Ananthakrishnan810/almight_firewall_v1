package com.example.firewall.utils;

public class ConstantsInUse {
 
    public static String url_user = "http://localhost:7080/auth";
    public static String encodedQuery = "id=1%20OR%201=1";
    public static String encodedXSS = "%3Cscript%3Ealert('XSS')%3C%2Fscript%3E";
    public static String encodedPath = "..%2F..%2F..%2Fetc%2Fpasswd";

}