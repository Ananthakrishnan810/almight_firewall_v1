package com.example.firewall.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.firewall.dto.UserMasterPojo;
import com.example.firewall.entity.RoleMasterEntity;
import com.example.firewall.entity.UserMasterEntity;
import com.example.firewall.repository.RoleMasterDao;
import com.example.firewall.repository.UserMasterDao;
import com.example.firewall.utils.ConstantsInUse;

import jakarta.annotation.PostConstruct;

@Service
public class AuthService {
    
    @Autowired
    private UserMasterDao userMasterDao;

    @Autowired
    private  RoleMasterDao roleMasterDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void ensureDefaultAdmin() {
        if (userMasterDao.count() == 0) {
            RoleMasterEntity adminRole = roleMasterDao.findByRoleName("ADMIN")
                    .orElseGet(() -> {
                        RoleMasterEntity r = new RoleMasterEntity();
                        r.setName("ADMIN");
                        return roleMasterDao.save(r);
                    });

            UserMasterEntity admin = new UserMasterEntity();
            admin.setUsername("secops_admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.fullName = "SecOps Admin";
            admin.email = "admin@netguard.local";
            admin.department = "Security Operations";
            admin.phone = "+1-000-0100";
            admin.setStatus(ConstantsInUse.ACTIVE_STATUS);
            admin.setRole(adminRole);
            userMasterDao.save(admin);
        }
    }

    public String register(UserMasterPojo request) {

        if (request.getUsername() == null || request.getUsername().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            return "Username and password are required";
        }

        if(userMasterDao.existsByUsername(request.getUsername().trim())){
            return "User already exists";
        }

        String roleName = (request.getRole() != null && !request.getRole().isBlank())
                ? request.getRole().trim().toUpperCase()
                : "USER";

        RoleMasterEntity role = roleMasterDao.findByRoleName(roleName)
                .orElseGet(() -> {
                    RoleMasterEntity newRole = new RoleMasterEntity();
                    newRole.setName(roleName);
                    return roleMasterDao.save(newRole);
                });

        UserMasterEntity user = new UserMasterEntity();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.fullName = (request.fullName != null && !request.fullName.isBlank()) ? request.fullName.trim() : user.getUsername();
        user.email = request.email != null ? request.email.trim() : "";
        user.department = (request.department != null && !request.department.isBlank()) ? request.department.trim() : "Security Operations";
        user.phone = request.phone != null ? request.phone.trim() : "";
        user.setStatus((request.status != null && !request.status.isBlank()) ? request.status.trim().toLowerCase() : ConstantsInUse.ACTIVE_STATUS);
        user.setRole(role);

        userMasterDao.save(user);

        return "User Registered Successfully";
    }

    public boolean isAdmin(String username) {
        if (username == null || username.isBlank()) return false;
        return userMasterDao.findByUsername(username)
                .map(u -> u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole().getName()))
                .orElse(false);
    }

    public List<UserMasterEntity> getAllUser(){

        List<UserMasterEntity> users = userMasterDao.findAll();
        return users != null ? users : List.of();

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

    public UserMasterEntity getByUserName(String username){

        return userMasterDao.findByUserName(username, ConstantsInUse.ACTIVE_STATUS);

    }

}