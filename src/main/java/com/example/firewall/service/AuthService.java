package com.example.firewall.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

<<<<<<< HEAD
import com.example.firewall.dto.UserMasterPojo;
import com.example.firewall.entity.RoleMasterEntity;
import com.example.firewall.entity.UserMasterEntity;
import com.example.firewall.repository.RoleMasterDao;
import com.example.firewall.repository.UserMasterDao;
=======
import com.example.firewall.dao.RoleMasterDao;
import com.example.firewall.dao.UserMasterDao;
import com.example.firewall.entity.RoleMasterEntity;
import com.example.firewall.entity.UserMasterEntity;
import com.example.firewall.pojo.UserMasterPojo;
>>>>>>> 0e72d3586e2d65d51679c159a5c63678f65035c2
import com.example.firewall.utils.ConstantsInUse;

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
<<<<<<< HEAD
=======
        user.setStatus(ConstantsInUse.ACTIVE_STATUS);
>>>>>>> 0e72d3586e2d65d51679c159a5c63678f65035c2

        RoleMasterEntity role = roleMasterDao.findByRoleName(request.getRole())
        .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRole(role);

        userMasterDao.save(user);

        return "User Registered Successfully";
    }

    public UserMasterEntity getAllUser(){

        UserMasterEntity user = userMasterDao.findAllUser(ConstantsInUse.ACTIVE_STATUS);
        return user;

    }

    public String deleteUserById(List<UserMasterPojo> userIds){

        for(UserMasterPojo user:userIds){
            UserMasterEntity userMasterEntity = userMasterDao.findUserList(user.username, ConstantsInUse.ACTIVE_STATUS);
            if(userMasterEntity != null){
                userMasterEntity.status = ConstantsInUse.INACTIVE_STATUS;
                userMasterDao.saveAndFlush(userMasterEntity);
            }
        }

        return ConstantsInUse.SUCCESS_MESSAGE;
    }

    public UserMasterEntity getUserDetails(String username){

        UserMasterEntity user = userMasterDao.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("user not found"));

        return user;
    }

<<<<<<< HEAD
    public UserMasterEntity getByUserName(String username){

        return userMasterDao.findByUserName(username, ConstantsInUse.ACTIVE_STATUS);

    }

=======
>>>>>>> 0e72d3586e2d65d51679c159a5c63678f65035c2
}