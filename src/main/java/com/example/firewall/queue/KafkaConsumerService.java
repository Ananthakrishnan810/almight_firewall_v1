package com.example.firewall.queue;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    
    @KafkaListener(topics = "firewall-logs", groupId = "firewall-group")
    public void consume(String message){
        System.out.println("Recived Message: "+ message);
    }
}
