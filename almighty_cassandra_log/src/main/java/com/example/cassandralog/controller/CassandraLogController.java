package com.example.cassandralog.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cassandralog.dto.CassandraLogPojo;
import com.example.cassandralog.dto.ClientRequestPojo;
import com.example.cassandralog.service.CassandraLogService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/cassandra-log")
public class CassandraLogController {

    @Autowired
    private CassandraLogService cassandraLogService;

    @PostMapping("/insert")
    public String insertLog(@RequestBody ClientRequestPojo requestPojo) {
        return cassandraLogService.insertLog(requestPojo);
    }

    @PostMapping("/save")
    public String saveLog(@RequestBody CassandraLogPojo pojo) {
        return cassandraLogService.saveCustomLog(pojo);
    }

    @GetMapping("/getAllLogs")
    public ResponseEntity<?> getAllLogs() {
        return ResponseEntity.ok(cassandraLogService.findAllLogs());
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(cassandraLogService.findLogById(id));
    }

    @GetMapping("/getByIp/{ip}")
    public ResponseEntity<?> getByIp(@PathVariable("ip") String ip) {
        return ResponseEntity.ok(cassandraLogService.findLogsByIp(ip));
    }

    @GetMapping("/getByThreatType/{threatType}")
    public ResponseEntity<?> getByThreatType(@PathVariable("threatType") String threatType) {
        return ResponseEntity.ok(cassandraLogService.findLogsByThreatType(threatType));
    }

    @GetMapping("/getByAction/{action}")
    public ResponseEntity<?> getByAction(@PathVariable("action") String action) {
        return ResponseEntity.ok(cassandraLogService.findLogsByAction(action));
    }

    @PostMapping("/delete")
    public String deleteLogs(@RequestBody List<UUID> logIds) {
        return cassandraLogService.deleteLogsById(logIds);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Almighty Cassandra Log Microservice is UP and RUNNING");
    }
}
