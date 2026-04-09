package com.example.firewall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.firewall.entity.RuleMasterEntity;

@Repository
public interface RuleMasterDao extends JpaRepository<RuleMasterEntity,Long>{

    @Query("select r from RuleMasterEntity r where r.pattern = :pattern and r.status = :status")
    RuleMasterEntity findRule(@Param("pattern") String pattern,@Param("status") String status);

    @Query("select r from RuleMasterEntity r where r.user.username = :username and r.status = :status")
    RuleMasterEntity findAllRule(@Param("username") String username,@Param("status") String status);

    
}