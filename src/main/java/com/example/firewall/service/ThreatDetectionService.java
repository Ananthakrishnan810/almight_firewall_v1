package com.example.firewall.service;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.firewall.utils.ConstantsInUse;

@Service
public class ThreatDetectionService {
 
    public WebClient webClient;

    public ThreatDetectionService() {
        this.webClient = WebClient.builder()
                .baseUrl(ConstantsInUse.PYTHON_CONNECTION)
                .build();
    }

    public String detectThreat(Map<String, String> payload) {

        Map response = webClient.post()
                .uri(ConstantsInUse.DETECT_API)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return (String) response.get("threat_type");
    }


}