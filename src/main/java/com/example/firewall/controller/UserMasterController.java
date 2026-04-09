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
<<<<<<< HEAD
import com.example.firewall.service.AuthService;
import com.example.firewall.dto.UserMasterPojo;
=======
import com.example.firewall.pojo.UserMasterPojo;
import com.example.firewall.service.AuthService;

>>>>>>> 0e72d3586e2d65d51679c159a5c63678f65035c2

@RestController
@RequestMapping("/users")
public class UserMasterController {
 
    @Autowired
    private AuthService authService;

<<<<<<< HEAD
=======

>>>>>>> 0e72d3586e2d65d51679c159a5c63678f65035c2
    @PostMapping("/register")
    public String registerUser(@RequestBody UserMasterPojo user){

        return authService.register(user);
        
    }

    @GetMapping("/getAllUsers")
<<<<<<< HEAD
    public ResponseEntity<?> fetchUser() {
=======
    public ResponseEntity<?> fetchUser(@RequestBody UserMasterPojo user) {
>>>>>>> 0e72d3586e2d65d51679c159a5c63678f65035c2
        
        return ResponseEntity.ok(authService.getAllUser());

    }
<<<<<<< HEAD

    @PostMapping("/delete")
    public String deleteUser(@RequestBody List<UserMasterPojo> userMasterPojos) {

        return authService.deleteUserById(userMasterPojos);    

    }
    
=======
    

    @PostMapping("/delete")
    public String deleteUser(@RequestBody List<UserMasterPojo> user) {

        return authService.deleteUserById(user);    

    }
    

>>>>>>> 0e72d3586e2d65d51679c159a5c63678f65035c2
    @GetMapping("/userDetails")
    public ResponseEntity<?> getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserMasterEntity user = authService.getUserDetails(username);
        return ResponseEntity.ok(user);
    }

}