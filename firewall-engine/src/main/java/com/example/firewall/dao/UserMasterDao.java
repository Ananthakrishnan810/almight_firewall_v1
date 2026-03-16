package com.example.firewall.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.firewall.entity.UserMasterEntity;

public interface UserMasterDao extends JpaRepository<UserMasterEntity,Long>{
 
    Optional<UserMasterEntity> findByUsername(String username);

    boolean existsByUsername(String username);

}