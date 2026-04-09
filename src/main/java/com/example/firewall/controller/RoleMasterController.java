package com.example.firewall.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

<<<<<<< HEAD
import com.example.firewall.entity.RoleMasterEntity;
import com.example.firewall.repository.RoleMasterDao;
=======
import com.example.firewall.dao.RoleMasterDao;
import com.example.firewall.entity.RoleMasterEntity;
>>>>>>> 0e72d3586e2d65d51679c159a5c63678f65035c2

@RestController
@RequestMapping("/roles")
public class RoleMasterController {
    
    @Autowired
    public RoleMasterDao roleMasterDao;

    @PostMapping("/create")
    public RoleMasterEntity createRole(@RequestBody RoleMasterEntity role){

        return roleMasterDao.save(role);

    }
}