package com.example.firewall.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.firewall.queue.KafkaConsumerService;

public class CassandraService {

    @Autowired
    private KafkaConsumerService kafkaConsumerService;

    
}