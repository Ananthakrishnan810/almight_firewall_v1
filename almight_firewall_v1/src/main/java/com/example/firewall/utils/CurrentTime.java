package com.example.firewall.utils;

import org.springframework.stereotype.Component;

@Component
public class CurrentTime {

    public long getCurrentTime(){
        
        long unixTimestamp = System.currentTimeMillis() / 1000;

        return unixTimestamp;

    }

}