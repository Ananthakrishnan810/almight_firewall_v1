package com.example.firewall.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.firewall.dao.RoleMasterDao;
import com.example.firewall.entity.RoleMasterEntity;


@RestController
@RequestMapping("/roles")
public class RoleMasterController {
    
    @Autowired
    private RoleMasterDao roleMasterDao;

    @PostMapping("/create")
    public RoleMasterEntity createRole(@RequestBody RoleMasterEntity role){

        return roleMasterDao.save(role);

    }
}