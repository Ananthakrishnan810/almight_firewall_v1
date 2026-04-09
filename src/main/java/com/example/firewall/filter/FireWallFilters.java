package com.example.firewall.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

@Component
public class FireWallFilters implements Filter{

    @Autowired
    public ThreatDetectionService threatDetectionService;

    @Autowired
    public CurrentTime currentTime;

    @Autowired
    public StringFormmer stringFormmer;

    @Autowired
    public KafkaProducerService kafkaProducerService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

                System.out.println("Firewall Triggered");

                Set<String> blockedIps = Set.of("192.168.1.10");

                String action = ConstantsInUse.REQUEST_NOT_ASSIGNED;
       
                HttpServletRequest req = (HttpServletRequest) request;
                HttpServletResponse res = (HttpServletResponse) response;
                long actionTime = currentTime.getCurrentTime();
                String ip = req.getRemoteAddr();
                String uri = req.getRequestId();
                String query = req.getQueryString();
                String method = req.getMethod();
                String body = request.getReader()
                                .lines()
                                .collect(Collectors.joining());

                Map<String, String> payload = new HashMap<>();
                payload.put("uri", uri);
                payload.put("query", query);
                payload.put("body", body);

                String threat_type = threatDetectionService.detectThreat(payload);

                if(blockedIps.contains(ip)){
                    res.sendError(403, "Blocked by Firewall");
                    return;
                }
                else
                { 
                    if(threat_type.equals("NONE"))
                    {
                        action = ConstantsInUse.ALLOW_THE_REQUEST;
                        getStringForRequestData(actionTime,ip,uri,query,method,threat_type,action);
                    }
                    else
                    {
                        action = ConstantsInUse.BLOCK_THE_REQUEST;
                        getStringForRequestData(actionTime,ip,uri, query, method, threat_type, action);
                    }
                }
                chain.doFilter(request, response);
    }
                        
    public void getStringForRequestData(long actionTime,String ip,String uri, String query, String method, String threat_type, String action) {

        ClientRequestPojo clientRequestPojo = new ClientRequestPojo();
        clientRequestPojo.copyClientRequestData(actionTime, ip, uri, query, method, threat_type, action);
        kafkaProducerService.sendMessage(clientRequestPojo);
     
    }

}