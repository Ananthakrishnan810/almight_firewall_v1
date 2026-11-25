# FireWall Engine Microservice (`almight_firewall_v1`)

A Spring Boot network security firewall service that intercepts incoming HTTP traffic, inspects requests against rules and machine learning threat models via the standalone **`threat_detection`** microservice, and streams security logs to **Apache Kafka**.

---

## Architecture Overview

```
                      Client Request
                            │
                            ▼
           [almight_firewall_v1 (Spring Boot - Port 7080)]
                            │
             (WebClient POST /detect)
                            ▼
          [threat_detection (FastAPI - Port 5000)]
                            │
            (ALLOW or BLOCK evaluation)
                            ▼
         (Produces ClientRequestPojo to Kafka)
                            │
            [Kafka Broker: localhost:9092]
                            │
         (Consumed by almighty_cassandra_log)
                            ▼
      [almighty_cassandra_log (Spring Boot - Port 7081)]
                            │
                 [Apache Cassandra 9042]
```

---

## Project Structure

```
almight_firewall_v1/
├── src/main/java/com/example/firewall/
│   ├── controller/      # REST API endpoints (/rule, /users, /roles, /threat, /auth)
│   ├── service/         # Business logic (RuleMasterService, ThreatDetectionService, AuthService)
│   ├── repository/      # Spring Data JPA repositories (RuleMasterDao, UserMasterDao)
│   ├── entity/          # JPA database entities (MySQL)
│   ├── dto/             # Data Transfer Objects (ClientRequestPojo, RuleMasterPojo, etc.)
│   ├── filter/          # FireWallFilters (Servlet filter intercepting HTTP traffic)
│   ├── queue/           # KafkaProducerService & KafkaConsumerService
│   ├── config/          # SecurityConfig, NettyServer
│   └── utils/           # ConstantsInUse, CurrentTime, StringFormmer
├── src/main/resources/
│   ├── application.properties
│   └── static/          # UI Dashboard assets
├── pom.xml
└── docker-compose.yml
```

---

## Inter-Service Communication

### Threat Detection Service (`threat_detection`)
- Communicates via Spring **WebClient** asynchronously.
- Configured in `application.properties`:
  ```properties
  threat.detection.url=http://localhost:5000
  threat.detection.api=/detect
  ```
- Gracefully handles timeouts and connection errors with fallback default `"NONE"` if `threat_detection` is temporarily unavailable.

### Kafka Event Streaming
- Streams structured `ClientRequestPojo` records to topic `firewall_logs` at `localhost:9092`.
- Consumed by [`almighty_cassandra_log`](../almighty_cassandra_log) for high-scale Cassandra audit storage.

---

## How to Run

### 1. Start External Infrastructure (Kafka, Zookeeper)
```bash
docker-compose up -d
```

### 2. Start the Threat Detection Service
```bash
cd ../threat_detection
./start.sh
```

### 3. Start the Firewall Spring Boot Service
```bash
mvn clean compile spring-boot:run
```
