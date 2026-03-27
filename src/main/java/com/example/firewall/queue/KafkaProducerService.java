package com.example.firewall.queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
 
    @Autowired
    public KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "firewall-logs";

    public void sendMessage(String message){
        kafkaTemplate.send(TOPIC, message);
    }


}