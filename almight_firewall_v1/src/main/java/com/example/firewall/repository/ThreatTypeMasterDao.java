package com.example.firewall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.firewall.entity.ThreatTypeMasterEntity;

@Repository
public interface  ThreatTypeMasterDao extends JpaRepository<ThreatTypeMasterEntity,Long>{
 
    @Query("select t from ThreatTypeMasterEntity t where t.threat_type = :threat_type and t.priority = :priority and t.status = :status")
    ThreatTypeMasterEntity findThreat(@Param("threat_type") String threat_type,@Param("priority") String priority,@Param("status") String status);

    @Query("select t from ThreatTypeMasterEntity t where t.id = :id and t.status = :status")
    ThreatTypeMasterEntity findThreatById(@Param("id") Long id,@Param("status") String status);

    @Query("select t from ThreatTypeMasterEntity t where t.status = :status")
    ThreatTypeMasterEntity findAllThreat(@Param("status") String status);

}