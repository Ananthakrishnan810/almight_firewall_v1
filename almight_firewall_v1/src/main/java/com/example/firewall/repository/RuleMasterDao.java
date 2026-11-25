package com.example.firewall.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.firewall.entity.RuleMasterEntity;

@Repository
public interface RuleMasterDao extends JpaRepository<RuleMasterEntity,Long>{

    @Query("select r from RuleMasterEntity r where r.pattern = :pattern and lower(r.status) = lower(:status)")
    RuleMasterEntity findRule(@Param("pattern") String pattern,@Param("status") String status);

    @Query("select r from RuleMasterEntity r where r.user.username = :username and lower(r.status) = lower(:status)")
    List<RuleMasterEntity> findAllRule(@Param("username") String username,@Param("status") String status);

    @Query("select r from RuleMasterEntity r where lower(r.status) = lower(:status)")
    List<RuleMasterEntity> findAllActiveRules(@Param("status") String status);

    @Query("select r from RuleMasterEntity r where r.status is null or lower(r.status) <> 'deleted'")
    List<RuleMasterEntity> findAllVisibleRules();

}