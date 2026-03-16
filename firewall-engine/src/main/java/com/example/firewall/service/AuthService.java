package com.example.firewall.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.firewall.dao.RoleMasterDao;
import com.example.firewall.dao.UserMasterDao;
import com.example.firewall.entity.RoleMasterEntity;
import com.example.firewall.entity.UserMasterEntity;
import com.example.firewall.pojo.UserMasterPojo;

@Service
public class AuthService {
    
    @Autowired
    private UserMasterDao userMasterDao;

    @Autowired
    private  RoleMasterDao roleMasterDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String register(UserMasterPojo request) {

        if(userMasterDao.existsByUsername(request.getUsername())){
            return "User already exists";
        }

        UserMasterEntity user = new UserMasterEntity();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        RoleMasterEntity role = roleMasterDao.findByRoleName(request.getRole())
        .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRole(role);

        userMasterDao.save(user);

        return "User Registered Successfully";
    }

}