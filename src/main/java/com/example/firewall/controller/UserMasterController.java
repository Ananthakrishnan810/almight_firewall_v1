package com.example.firewall.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.firewall.pojo.UserMasterPojo;
import com.example.firewall.service.AuthService;

@RestController
@RequestMapping("/users")
public class UserMasterController {
 
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public String registerUser(@RequestBody UserMasterPojo user){

        return authService.register(user);
        
    }
}