package com.example.cassandralog.queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.cassandralog.dto.ClientRequestPojo;
import com.example.cassandralog.service.CassandraLogService;
import com.example.cassandralog.utils.ConstantsInUse;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class KafkaConsumerService {

    @Autowired
    private CassandraLogService cassandraLogService;

    @Autowired(required = false)
    private MeterRegistry meterRegistry;

    @KafkaListener(topics = ConstantsInUse.TOPIC, groupId = "cassandra-log-group")
    public void consume(ClientRequestPojo message) {
        System.out.println("Cassandra Log Microservice - Consumed message from Kafka: " + message);
        cassandraLogService.insertLog(message);
        if (meterRegistry != null && message != null) {
            String threat = message.threat_type != null ? message.threat_type : "NONE";
            String action = message.action != null ? message.action : "UNKNOWN";
            meterRegistry.counter("cassandra_logs_ingested_total", "threat_type", threat, "action", action).increment();
        }
    }
}
