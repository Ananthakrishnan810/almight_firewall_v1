package com.example.firewall.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.firewall.entity.RoleMasterEntity;

public interface RoleMasterDao extends JpaRepository<RoleMasterEntity, Long>{
 
    Optional<RoleMasterEntity> findByRoleName(String roleName);

}