package com.example.firewall.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.firewall.entity.RoleMasterEntity;

@Repository
public interface RoleMasterDao extends JpaRepository<RoleMasterEntity, Long>{
 
    Optional<RoleMasterEntity> findByRoleName(String roleName);

}