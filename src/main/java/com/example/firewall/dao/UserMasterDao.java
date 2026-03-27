package com.example.firewall.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.firewall.entity.UserMasterEntity;

@Repository
public interface UserMasterDao extends JpaRepository<UserMasterEntity,Long>{
 
    Optional<UserMasterEntity> findByUsername(String username);

    boolean existsByUsername(String username);

}