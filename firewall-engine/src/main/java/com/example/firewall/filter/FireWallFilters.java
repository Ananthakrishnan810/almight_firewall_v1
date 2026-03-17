    package com.example.firewall.filter;

import java.io.IOException;
import java.util.Set;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FireWallFilters implements Filter{

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

                Set<String> blockedIps = Set.of("192.168.1.10");
       
                HttpServletRequest req = (HttpServletRequest) response;
                String ip = req.getRemoteAddr();

                if(blockedIps.contains(ip)){
                    HttpServletResponse res = (HttpServletResponse) response;
                    res.sendError(403, "Blocked by Firewall");
                    return;
                }
                chain.doFilter(request, response);
    }

}