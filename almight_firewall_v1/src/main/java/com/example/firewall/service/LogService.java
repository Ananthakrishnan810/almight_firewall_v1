package com.example.firewall.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public LogService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendLog(String log) {
        kafkaTemplate.send("firewall-logs", log);
    }
}