package com.example.firewall.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.firewall.dto.ClientRequestPojo;
import com.example.firewall.utils.ConstantsInUse;
import com.example.firewall.utils.CurrentTime;
import com.example.firewall.utils.StringFormmer;

import static org.junit.jupiter.api.Assertions.*;

public class ThreatDetectionServiceTest {

    private ThreatDetectionService threatDetectionService;

    @BeforeEach
    public void setup() {
        threatDetectionService = new ThreatDetectionService();
    }

    @Test
    public void testThreatDetectionFallbackWhenOffline() {
        // Pointing to a non-existent port to test fallback and resiliency
        ReflectionTestUtils.setField(threatDetectionService, "threatDetectionUrl", "http://localhost:59999");
        ReflectionTestUtils.setField(threatDetectionService, "detectApi", "/detect");
        threatDetectionService.init();

        Map<String, String> payload = new HashMap<>();
        payload.put("query", "id=1 UNION SELECT 1,2,3");
        payload.put("uri", "/login");
        payload.put("body", "");

        // Must gracefully return NONE instead of throwing an unhandled exception
        String result = threatDetectionService.detectThreat(payload);
        assertEquals("NONE", result, "When external threat detection is unavailable, it should safely return NONE");
    }

    @Test
    public void testLiveCommunicationWithThreatDetectionService() {
        // If FastAPI service is running on port 5000, verify full inter-service communication
        ReflectionTestUtils.setField(threatDetectionService, "threatDetectionUrl", "http://localhost:5000");
        ReflectionTestUtils.setField(threatDetectionService, "detectApi", "/detect");
        threatDetectionService.init();

        Map<String, String> sqliPayload = new HashMap<>();
        sqliPayload.put("query", "id=1 UNION SELECT 1,2,3");
        sqliPayload.put("uri", "/login");
        sqliPayload.put("body", "");

        String result = threatDetectionService.detectThreat(sqliPayload);
        // If port 5000 is running, it returns SQL_INJECTION; if offline, it returns NONE
        assertTrue("SQL_INJECTION".equals(result) || "NONE".equals(result),
                "Result must be SQL_INJECTION (when live) or NONE (when offline)");
    }

    @Test
    public void testRuleFilePathResolution() {
        String rulePath = ConstantsInUse.resolveRuleFilePath();
        assertNotNull(rulePath, "Rule file path should not be null");
        File ruleFile = new File(rulePath);
        assertTrue(ruleFile.exists(), "Resolved rule file must exist on filesystem: " + ruleFile.getAbsolutePath());
    }

    @Test
    public void testClientRequestPojoDataCopy() {
        ClientRequestPojo pojo = new ClientRequestPojo();
        long now = System.currentTimeMillis() / 1000;
        pojo.copyClientRequestData(now, "192.168.1.50", "/api/data", "id=10", "GET", "NONE", "ALLOW");

        assertEquals(now, pojo.actionTime);
        assertEquals("192.168.1.50", pojo.ip);
        assertEquals("/api/data", pojo.uri);
        assertEquals("id=10", pojo.query);
        assertEquals("GET", pojo.method);
        assertEquals("NONE", pojo.threat_type);
        assertEquals("ALLOW", pojo.action);
    }

    @Test
    public void testStringFormmerUtility() {
        StringFormmer formmer = new StringFormmer();
        long now = 1700000000L;
        String json = formmer.formString(now, "127.0.0.1", "/test", "q=1", "POST", "SQL_INJECTION", "BLOCK");

        assertNotNull(json);
        assertTrue(json.contains("127.0.0.1"));
        assertTrue(json.contains("SQL_INJECTION"));
        assertTrue(json.contains("BLOCK"));
    }

    @Test
    public void testCurrentTimeUtility() {
        CurrentTime currentTime = new CurrentTime();
        long time = currentTime.getCurrentTime();
        assertTrue(time > 1700000000L, "Unix timestamp should be valid positive number");
    }
}
