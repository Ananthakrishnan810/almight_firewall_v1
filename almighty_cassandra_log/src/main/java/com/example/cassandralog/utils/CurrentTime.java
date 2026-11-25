package com.example.cassandralog.utils;

import org.springframework.stereotype.Component;

@Component
public class CurrentTime {

    public long getCurrentTime() {
        return System.currentTimeMillis() / 1000;
    }
}
