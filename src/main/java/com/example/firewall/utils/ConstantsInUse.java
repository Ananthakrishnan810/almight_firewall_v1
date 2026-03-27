package com.example.firewall.utils;

public class ConstantsInUse {
 
    public static String URL_USE = "http://localhost:7080/auth";
    public static String ENCODEDQUERY = "id=1%20OR%201=1";
    public static String ENCODEDXSS = "%3Cscript%3Ealert('XSS')%3C%2Fscript%3E";
    public static String ENCODEDPATH = "..%2F..%2F..%2Fetc%2Fpasswd";

}