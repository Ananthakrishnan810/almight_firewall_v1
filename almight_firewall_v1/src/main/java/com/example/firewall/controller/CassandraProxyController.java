package com.example.firewall.controller;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/cassandra-proxy")
public class CassandraProxyController {

    private static final Logger logger = LoggerFactory.getLogger(CassandraProxyController.class);

    @Value("${cassandra.log.url:http://localhost:7081/cassandra-log}")
    private String cassandraLogBaseUrl;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(cassandraLogBaseUrl)
                .build();
        logger.info("CassandraProxyController initialized with base URL: {}", cassandraLogBaseUrl);
    }

    @GetMapping("/getAllLogs")
    public ResponseEntity<?> getAllLogs() {
        try {
            List<?> logs = webClient.get()
                    .uri("/getAllLogs")
                    .retrieve()
                    .bodyToMono(List.class)
                    .timeout(Duration.ofSeconds(4))
                    .block();
            return ResponseEntity.ok(logs != null ? logs : Collections.emptyList());
        } catch (Exception e) {
            logger.warn("Could not retrieve logs from Cassandra microservice: {}", e.getMessage());
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/getByIp/{ip}")
    public ResponseEntity<?> getByIp(@PathVariable("ip") String ip) {
        try {
            List<?> logs = webClient.get()
                    .uri("/getByIp/" + ip)
                    .retrieve()
                    .bodyToMono(List.class)
                    .timeout(Duration.ofSeconds(4))
                    .block();
            return ResponseEntity.ok(logs != null ? logs : Collections.emptyList());
        } catch (Exception e) {
            logger.warn("Could not retrieve logs by IP: {}", e.getMessage());
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/getByThreatType/{threatType}")
    public ResponseEntity<?> getByThreatType(@PathVariable("threatType") String threatType) {
        try {
            List<?> logs = webClient.get()
                    .uri("/getByThreatType/" + threatType)
                    .retrieve()
                    .bodyToMono(List.class)
                    .timeout(Duration.ofSeconds(4))
                    .block();
            return ResponseEntity.ok(logs != null ? logs : Collections.emptyList());
        } catch (Exception e) {
            logger.warn("Could not retrieve logs by ThreatType: {}", e.getMessage());
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/getByAction/{action}")
    public ResponseEntity<?> getByAction(@PathVariable("action") String action) {
        try {
            List<?> logs = webClient.get()
                    .uri("/getByAction/" + action)
                    .retrieve()
                    .bodyToMono(List.class)
                    .timeout(Duration.ofSeconds(4))
                    .block();
            return ResponseEntity.ok(logs != null ? logs : Collections.emptyList());
        } catch (Exception e) {
            logger.warn("Could not retrieve logs by Action: {}", e.getMessage());
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteLogs(@RequestBody List<?> ids) {
        try {
            String res = webClient.post()
                    .uri("/delete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(ids)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(4))
                    .block();
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error proxying delete to Cassandra log service: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        try {
            String res = webClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(2))
                    .block();
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.ok("UNAVAILABLE");
        }
    }
}
