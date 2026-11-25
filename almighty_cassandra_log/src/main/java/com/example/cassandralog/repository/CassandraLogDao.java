package com.example.cassandralog.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.cassandralog.entity.CassandraLogEntity;

@Repository
public interface CassandraLogDao extends CassandraRepository<CassandraLogEntity, UUID> {

    @Query("SELECT * FROM firewall_logs WHERE ip = :ip ALLOW FILTERING")
    List<CassandraLogEntity> findByIp(@Param("ip") String ip);

    @Query("SELECT * FROM firewall_logs WHERE threat_type = :threatType ALLOW FILTERING")
    List<CassandraLogEntity> findByThreatType(@Param("threatType") String threatType);

    @Query("SELECT * FROM firewall_logs WHERE action = :action ALLOW FILTERING")
    List<CassandraLogEntity> findByAction(@Param("action") String action);

    @Query("SELECT * FROM firewall_logs WHERE status = :status ALLOW FILTERING")
    List<CassandraLogEntity> findByStatus(@Param("status") String status);
}
