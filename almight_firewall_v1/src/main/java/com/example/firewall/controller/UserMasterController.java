package com.example.firewall.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.firewall.entity.UserMasterEntity;
import com.example.firewall.service.AuthService;
import com.example.firewall.dto.UserMasterPojo;

@RestController
@RequestMapping("/users")
public class UserMasterController {
 
    @Autowired
    private AuthService authService;

    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return false;
        }
        boolean hasAuthority = auth.getAuthorities().stream()
                .anyMatch(a -> "ADMIN".equalsIgnoreCase(a.getAuthority()) || "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));
        return hasAuthority || authService.isAdmin(auth.getName());
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserMasterPojo user){
        if (!isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: Only ADMIN users have authority to create users");
        }
        return ResponseEntity.ok(authService.register(user));
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<?> fetchUser() {
        if (!isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: Admin authority required");
        }
        return ResponseEntity.ok(authService.getAllUser());
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestBody List<UserMasterPojo> userMasterPojos) {
        if (!isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: Only ADMIN users can delete users");
        }
        return ResponseEntity.ok(authService.deleteUserById(userMasterPojos));
    }
    
    @GetMapping("/userDetails")
    public ResponseEntity<?> getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserMasterEntity user = authService.getUserDetails(username);
        return ResponseEntity.ok(user);
    }

}