package com.example.firewall.queue;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.firewall.dto.ClientRequestPojo;

@Service
public class KafkaConsumerService {
    
    @KafkaListener(topics = "firewall_logs", groupId = "firewall_logs")
    public void consume(ClientRequestPojo message){
        System.out.println("Recived Message: "+ message);
    }
}
