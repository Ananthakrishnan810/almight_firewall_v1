package com.example.firewall.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.firewall.entity.UserMasterEntity;

@Repository
public interface UserMasterDao extends JpaRepository<UserMasterEntity,Long>{
 
    Optional<UserMasterEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("select u from UserMasterEntity u where u.username = :username and u.status = :status")
    UserMasterEntity findUserList(@Param("username") String username,@Param("status") String status);

    @Query("select u from UserMasterEntity u where u.status = :status")
    UserMasterEntity findAllUser(@Param("status") String status);

}