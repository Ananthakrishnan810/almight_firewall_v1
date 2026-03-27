package com.example.firewall.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.firewall.pojo.LoginMasterPojo;

@RestController
@RequestMapping("/auth")
public class LoginMasterController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public String login(@RequestBody LoginMasterPojo login){

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                            login.getUsername(),
                            login.getPassword()
                        )
                );

        if(authentication.isAuthenticated()){
            return "Login Successful";
        }

        return "Invalid Credentials";
    }

    @PostMapping
    public String auth() {
        return "Auth endpoint working";
    }

}