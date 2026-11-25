package com.example.firewall.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.firewall.dto.ClientRequestPojo;
import com.example.firewall.queue.KafkaProducerService;
import com.example.firewall.service.ThreatDetectionService;
import com.example.firewall.utils.ConstantsInUse;
import com.example.firewall.utils.CurrentTime;
import com.example.firewall.utils.StringFormmer;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class FireWallFilters implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(FireWallFilters.class);

    @Autowired
    public ThreatDetectionService threatDetectionService;

    @Autowired
    public CurrentTime currentTime;

    @Autowired
    public StringFormmer stringFormmer;

    @Autowired
    public KafkaProducerService kafkaProducerService;

    @Autowired(required = false)
    private MeterRegistry meterRegistry;

    private static final Set<String> BLOCKED_IPS = Set.of("192.168.1.10");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Skip filtering for static web assets (css, js, html, images) and internal management endpoints
        String requestPath = req.getRequestURI();
        if (requestPath.equals("/") ||
                requestPath.endsWith(".css") || requestPath.endsWith(".js") ||
                requestPath.endsWith(".html") || requestPath.endsWith(".ico") ||
                requestPath.endsWith(".png") || requestPath.endsWith(".jpg") ||
                requestPath.endsWith(".svg") ||
                requestPath.startsWith("/static") ||
                requestPath.startsWith("/actuator") ||
                requestPath.startsWith("/simulate") ||
                requestPath.startsWith("/rule") ||
                requestPath.startsWith("/threat") ||
                requestPath.startsWith("/users") ||
                requestPath.startsWith("/roles") ||
                requestPath.equals("/auth/login") ||
                requestPath.equals("/auth/me") ||
                requestPath.equals("/auth/logout") ||
                requestPath.startsWith("/cassandra-proxy")) {
            chain.doFilter(request, response);
            return;
        }

        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(req);

        long actionTime = currentTime.getCurrentTime();
        String ip = wrappedRequest.getRemoteAddr();
        String uri = wrappedRequest.getRequestURI();
        String query = wrappedRequest.getQueryString();
        String method = wrappedRequest.getMethod();
        String body = wrappedRequest.getBodyAsString();

        Map<String, String> payload = new HashMap<>();
        payload.put("uri", uri);
        payload.put("query", query != null ? query : "");
        payload.put("body", body != null ? body : "");

        logger.info("[Firewall Inspection] Method: {}, URI: {}, Query: {}, IP: {}", method, uri, query, ip);

        // 1. Check IP Blacklist
        if (BLOCKED_IPS.contains(ip)) {
            logger.warn("[Firewall Blocked] IP {} is in blocked list", ip);
            String action = ConstantsInUse.BLOCK_THE_REQUEST;
            sendRequestLog(actionTime, ip, uri, query, method, "IP_BLACKLISTED", action);
            if (meterRegistry != null) {
                meterRegistry.counter("firewall_requests_total", "action", "BLOCK", "threat_type", "IP_BLACKLISTED").increment();
            }

            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json");
            res.getWriter().write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Blocked by Firewall: IP " + ip + " is blacklisted\"}");
            return;
        }

        // 2. Perform Threat Detection via Python ML/Regex service
        String threatType = threatDetectionService.detectThreat(payload);

        // 3. Evaluate ALLOW vs BLOCK
        if (threatType == null || "NONE".equalsIgnoreCase(threatType)) {
            logger.info("[Firewall Allowed] URI: {}, Query: {}", uri, query);
            String action = ConstantsInUse.ALLOW_THE_REQUEST;
            sendRequestLog(actionTime, ip, uri, query, method, "NONE", action);
            if (meterRegistry != null) {
                meterRegistry.counter("firewall_requests_total", "action", "ALLOW", "threat_type", "NONE").increment();
            }

            chain.doFilter(wrappedRequest, response);
        } else {
            logger.warn("[Firewall Blocked] Threat Detected: '{}' on URI: {}, Query: {}", threatType, uri, query);
            String action = ConstantsInUse.BLOCK_THE_REQUEST;
            sendRequestLog(actionTime, ip, uri, query, method, threatType, action);
            if (meterRegistry != null) {
                meterRegistry.counter("firewall_requests_total", "action", "BLOCK", "threat_type", threatType).increment();
            }

            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json");
            res.getWriter().write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Blocked by Firewall: Threat detected - " + threatType + "\"}");
        }
    }

    public void sendRequestLog(long actionTime, String ip, String uri, String query, String method, String threatType, String action) {
        try {
            ClientRequestPojo clientRequestPojo = new ClientRequestPojo();
            clientRequestPojo.copyClientRequestData(actionTime, ip, uri, query, method, threatType, action);
            kafkaProducerService.sendMessage(clientRequestPojo);
        } catch (Exception e) {
            logger.error("Failed to push firewall log event to Kafka: {}", e.getMessage());
        }
    }
}
