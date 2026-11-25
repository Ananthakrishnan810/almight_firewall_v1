package com.example.firewall.service;

/**
 * NOTE: Cassandra persistence has been migrated to the dedicated microservice:
 * 'almighty_cassandra_log' (com.example.cassandralog.service.CassandraLogService).
 * 
 * almight_firewall_v1 delegates all log persistence to almighty_cassandra_log
 * via the Kafka topic 'firewall_logs'.
 */
public class CassandraService {
}
