package com.example.firewall.service;

import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.firewall.utils.ConstantsInUse;

import jakarta.annotation.PostConstruct;

@Service
public class ThreatDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(ThreatDetectionService.class);

    @Value("${threat.detection.url:http://localhost:5000}")
    private String threatDetectionUrl;

    @Value("${threat.detection.api:/detect}")
    private String detectApi;

    public WebClient webClient;

    public ThreatDetectionService() {
    }

    @PostConstruct
    public void init() {
        if (threatDetectionUrl == null || threatDetectionUrl.isEmpty()) {
            threatDetectionUrl = ConstantsInUse.PYTHON_CONNECTION;
        }
        this.webClient = WebClient.builder()
                .baseUrl(threatDetectionUrl)
                .build();
        logger.info("ThreatDetectionService initialized with baseUrl: {}", threatDetectionUrl);
    }

    public String detectThreat(Map<String, String> payload) {
        try {
            if (this.webClient == null) {
                init();
            }

            String targetUri = (detectApi != null && !detectApi.isEmpty()) ? detectApi : ConstantsInUse.DETECT_API;

            Map response = webClient.post()
                    .uri(targetUri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();

            if (response != null && response.containsKey("threat_type")) {
                return (String) response.get("threat_type");
            }
        } catch (Exception e) {
            logger.warn("Threat Detection Service at {} unavailable or timed out: {}. Defaulting to NONE.",
                    threatDetectionUrl, e.getMessage());
        }

        return "NONE";
    }
}
