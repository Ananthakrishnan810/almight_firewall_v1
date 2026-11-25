package com.example.firewall.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.firewall.dto.ClientRequestPojo;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired(required = false)
    public KafkaTemplate<ClientRequestPojo, ClientRequestPojo> kafkaTemplate;

    private static final String TOPIC = "firewall_logs";

    public void sendMessage(ClientRequestPojo message) {
        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send(TOPIC, message).whenComplete((result, ex) -> {
                    if (ex != null) {
                        logger.warn("Kafka broker not reachable at localhost:9092; failed to publish log: {}", ex.getMessage());
                    }
                });
            } catch (Exception e) {
                logger.warn("Error sending message to Kafka topic '{}': {}", TOPIC, e.getMessage());
            }
        }
    }
}
