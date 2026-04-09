package com.example.firewall.queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.firewall.dto.ClientRequestPojo;

@Service
public class KafkaProducerService {
 
    @Autowired
    public KafkaTemplate<ClientRequestPojo, ClientRequestPojo> kafkaTemplate;

    private static final String TOPIC = "firewall_logs";

    public void sendMessage(ClientRequestPojo message){
        kafkaTemplate.send(TOPIC, message);
    }

}