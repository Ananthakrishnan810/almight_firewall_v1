package com.example.firewall.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.firewall.utils.ConstantsInUse;
import com.example.firewall.utils.Simulator;

@RestController
@RequestMapping("/simulate")
public class SimulationController {

    @GetMapping("/run")
    public ResponseEntity<?> runSimulation(
            @RequestParam(value = "url", required = false) String targetUrl,
            @RequestParam(value = "repeat", defaultValue = "1") int repeat,
            @RequestParam(value = "vector", defaultValue = "ALL") String vector) {

        String url = (targetUrl != null && !targetUrl.isEmpty()) ? targetUrl : ConstantsInUse.URL_USE;

        List<Simulator.Attack> allAttacks = List.of(
                new Simulator.Attack("SQL Injection (Query)", url + "?query=" + ConstantsInUse.ENCODEDQUERY, "POST", "{}"),
                new Simulator.Attack("SQL Injection (Body)", url, "POST", "{\"query\":\"" + ConstantsInUse.ENCODEDQUERY + "\"}"),
                new Simulator.Attack("XSS Attack", url + "?query=" + ConstantsInUse.ENCODEDXSS, "POST", "{}"),
                new Simulator.Attack("Path Traversal", url + "?query=" + ConstantsInUse.ENCODEDPATH, "POST", "{}"),
                new Simulator.Attack("Normal Clean Request", url, "POST", "{\"username\":\"john_doe\"}")
        );

        String vec = vector != null ? vector.trim().toUpperCase() : "ALL";
        List<Simulator.Attack> attackList = switch (vec) {
            case "SQLI" -> allAttacks.stream().filter(a -> a.name.startsWith("SQL")).toList();
            case "XSS" -> allAttacks.stream().filter(a -> a.name.startsWith("XSS")).toList();
            case "PATH" -> allAttacks.stream().filter(a -> a.name.startsWith("Path")).toList();
            case "CLEAN" -> allAttacks.stream().filter(a -> a.name.startsWith("Normal")).toList();
            default -> allAttacks;
        };

        List<Simulator.SimulationResult> results = new ArrayList<>();
        int blocked = 0;
        int allowed = 0;
        int failed = 0;

        for (int i = 0; i < repeat; i++) {
            for (Simulator.Attack attack : attackList) {
                Simulator.SimulationResult res = Simulator.executeAttack(attack);
                results.add(res);
                if (res.responseCode == 403) {
                    blocked++;
                } else if (res.responseCode == 200) {
                    allowed++;
                } else {
                    failed++;
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "completed");
        response.put("targetUrl", url);
        response.put("totalRequests", results.size());
        response.put("blockedCount", blocked);
        response.put("allowedCount", allowed);
        response.put("failedCount", failed);
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "Attack & Traffic Simulator");
        status.put("targetUrl", ConstantsInUse.URL_USE);
        status.put("availableVectors", List.of(
                "SQL Injection (Query)",
                "SQL Injection (Body)",
                "XSS Attack",
                "Path Traversal",
                "Normal Clean Request"
        ));
        return ResponseEntity.ok(status);
    }
}
