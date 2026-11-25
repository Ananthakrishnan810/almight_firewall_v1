package com.example.firewall.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.firewall.dto.LoginMasterPojo;
import com.example.firewall.entity.UserMasterEntity;
import com.example.firewall.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/auth")
public class LoginMasterController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginMasterPojo login, HttpServletRequest request){
        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                login.getUsername(),
                                login.getPassword()
                            )
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            UserMasterEntity user = authService.getUserDetails(login.username);
            return ResponseEntity.ok(toSessionSummary(user));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "username", "viewer",
                    "fullName", "Demo Viewer",
                    "role", "VIEWER",
                    "isAdmin", false
            ));
        }
        UserMasterEntity user = authService.getUserDetails(auth.getName());
        return ResponseEntity.ok(toSessionSummary(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("status", "logged_out"));
    }

    private Map<String, Object> toSessionSummary(UserMasterEntity user) {
        String roleName = (user.getRole() != null && user.getRole().getName() != null)
                ? user.getRole().getName().toUpperCase()
                : "USER";
        Map<String, Object> map = new HashMap<>();
        map.put("authenticated", true);
        map.put("username", user.getUsername());
        map.put("fullName", user.fullName != null ? user.fullName : user.getUsername());
        map.put("email", user.email != null ? user.email : "");
        map.put("department", user.department != null ? user.department : "");
        map.put("role", roleName);
        map.put("isAdmin", "ADMIN".equalsIgnoreCase(roleName));
        return map;
    }

    @PostMapping
    public String auth() {
        return "Auth endpoint working";
    }

}