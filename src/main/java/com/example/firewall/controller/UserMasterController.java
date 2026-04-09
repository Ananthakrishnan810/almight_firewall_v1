package com.example.firewall.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/register")
    public String registerUser(@RequestBody UserMasterPojo user){

        return authService.register(user);
        
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<?> fetchUser() {
        
        return ResponseEntity.ok(authService.getAllUser());

    }

    @PostMapping("/delete")
    public String deleteUser(@RequestBody List<UserMasterPojo> userMasterPojos) {

        return authService.deleteUserById(userMasterPojos);    

    }
    
    @GetMapping("/userDetails")
    public ResponseEntity<?> getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserMasterEntity user = authService.getUserDetails(username);
        return ResponseEntity.ok(user);
    }

}