package com.example.firewall.queue;

import com.example.firewall.dto.ClientRequestPojo;

/**
 * NOTE: Kafka Consumer logic has been migrated to the dedicated microservice:
 * 'almighty_cassandra_log' (com.example.cassandralog.queue.KafkaConsumerService).
 * 
 * In this microservice (almight_firewall_v1), only KafkaProducerService is active
 * to stream log events to the 'firewall_logs' topic.
 * 
 * If you ever need a local debug listener here, uncomment @Service and @KafkaListener below.
 */
// @org.springframework.stereotype.Service
public class KafkaConsumerService {
    
    // @org.springframework.kafka.annotation.KafkaListener(topics = "firewall_logs", groupId = "firewall_logs")
    public void consume(ClientRequestPojo message){
        System.out.println("Received Message in firewall_v1: " + message);
    }
}
