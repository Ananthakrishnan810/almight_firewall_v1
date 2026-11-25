# Almighty Cassandra Log Microservice

A dedicated Spring Boot microservice designed to consume, persist, query, and manage firewall security logs in **Apache Cassandra** streamed via **Apache Kafka**.

---

## 1. Overview & Architecture

This microservice is part of the **Almighty Firewall** distributed architecture:
- **`almight_firewall_v1`**: Intercepts HTTP traffic, performs ML/rule-based threat detection, determines ALLOW/BLOCK decisions, and produces `ClientRequestPojo` events to Kafka topic `firewall_logs`.
- **`almighty_cassandra_log` (This Service)**: Consumes `ClientRequestPojo` events from the Kafka topic `firewall_logs` and writes high-throughput, partitioned audit logs into Apache Cassandra. It exposes REST APIs for querying logs by IP, threat type, action, ID, and performing log lifecycle operations.

```
Incoming Request -> [almight_firewall_v1] (Port 7080)
                           |
                     (Produces to Kafka Topic: "firewall_logs")
                           v
               [Apache Kafka Broker: 9092]
                           v
                     (Consumes via KafkaListener)
                           |
              [almighty_cassandra_log] (Port 7081)
                     |                   |
                     v                   v
            [Apache Cassandra: 9042]    [REST API: /cassandra-log/*]
```

---

## 2. API Endpoints

Base URL: `http://localhost:7081/cassandra-log`

| Method | Endpoint | Description | Request Body / Param | Response |
|---|---|---|---|---|
| `GET` | `/health` | Health check endpoint | None | `200 OK` (Status text) |
| `POST` | `/insert` | Ingest log via `ClientRequestPojo` | `ClientRequestPojo` (JSON) | `"success"` / `"failure"` |
| `POST` | `/save` | Save log via `CassandraLogPojo` | `CassandraLogPojo` (JSON) | `"success"` / `"failure"` |
| `GET` | `/getAllLogs` | Retrieve all firewall audit logs | None | `200 OK` (JSON List of logs) |
| `GET` | `/getById/{id}` | Retrieve specific log by UUID | `id` (Path variable) | `200 OK` (JSON log) |
| `GET` | `/getByIp/{ip}` | Query logs matching client IP | `ip` (Path variable) | `200 OK` (JSON List) |
| `GET` | `/getByThreatType/{threatType}` | Query logs by threat type | `threatType` (Path variable) | `200 OK` (JSON List) |
| `GET` | `/getByAction/{action}` | Query logs by action (ALLOW / BLOCK) | `action` (Path variable) | `200 OK` (JSON List) |
| `POST` | `/delete` | Soft delete logs (status: inactive) | `List<UUID>` (JSON array) | `"success"` / `"failure"` |

---

## 3. Cassandra Schema

- **Keyspace**: `firewall_key`
- **Replication**: `{'class': 'SimpleStrategy', 'replication_factor': 1}`
- **Table**: `firewall_logs`

```sql
CREATE KEYSPACE IF NOT EXISTS firewall_key 
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1} 
AND durable_writes = true;

CREATE TABLE IF NOT EXISTS firewall_key.firewall_logs (
    id UUID PRIMARY KEY,
    action_time bigint,
    ip text,
    uri text,
    query text,
    method text,
    threat_type text,
    action text,
    status text
);
```

---

## 4. Configuration (`application.properties`)

```properties
server.port=7081

# Cassandra Cluster
spring.cassandra.contact-points=localhost
spring.cassandra.port=9042
spring.cassandra.keyspace-name=firewall_key
spring.cassandra.local-datacenter=datacenter1
spring.cassandra.schema-action=CREATE_IF_NOT_EXISTS

# Kafka Broker & Consumer
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=cassandra-log-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
spring.kafka.consumer.properties.spring.json.value.default.type=com.example.cassandralog.dto.ClientRequestPojo
```

---

## 5. How to Run

### Step 1: Start Cassandra and Kafka infrastructure
```bash
docker-compose up -d
```

### Step 2: Build and Run Spring Boot Application
```bash
mvn clean compile spring-boot:run
```
Or package as a JAR:
```bash
mvn clean package -DskipTests
java -jar target/almighty-cassandra-log-1.0.0.jar
```
