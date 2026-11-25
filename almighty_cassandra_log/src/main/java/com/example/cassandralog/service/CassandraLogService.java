package com.example.cassandralog.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.cassandralog.dto.CassandraLogPojo;
import com.example.cassandralog.dto.ClientRequestPojo;
import com.example.cassandralog.entity.CassandraLogEntity;
import com.example.cassandralog.repository.CassandraLogDao;
import com.example.cassandralog.utils.ConstantsInUse;
import com.example.cassandralog.utils.CurrentTime;

@Service
public class CassandraLogService {

    @Autowired
    private CassandraLogDao cassandraLogDao;

    @Autowired
    private CurrentTime currentTime;

    public String insertLog(ClientRequestPojo requestPojo) {
        try {
            CassandraLogEntity logEntity = new CassandraLogEntity();
            logEntity.id = UUID.randomUUID();
            logEntity.actionTime = (requestPojo.actionTime != null) ? requestPojo.actionTime : currentTime.getCurrentTime();
            logEntity.ip = requestPojo.ip;
            logEntity.uri = requestPojo.uri;
            logEntity.query = requestPojo.query;
            logEntity.method = requestPojo.method;
            logEntity.threat_type = requestPojo.threat_type;
            logEntity.action = requestPojo.action;
            logEntity.status = ConstantsInUse.ACTIVE_STATUS;

            cassandraLogDao.save(logEntity);


            
            return ConstantsInUse.SUCCESS_MESSAGE;
        } catch (Exception e) {
            e.printStackTrace();
            return ConstantsInUse.FAILURE_MESSAGE;
        }
    }

    public String saveCustomLog(CassandraLogPojo pojo) {
        try {
            CassandraLogEntity logEntity = new CassandraLogEntity();
            logEntity.id = (pojo.id != null) ? pojo.id : UUID.randomUUID();
            logEntity.actionTime = (pojo.actionTime != null) ? pojo.actionTime : currentTime.getCurrentTime();
            logEntity.ip = pojo.ip;
            logEntity.uri = pojo.uri;
            logEntity.query = pojo.query;
            logEntity.method = pojo.method;
            logEntity.threat_type = pojo.threat_type;
            logEntity.action = pojo.action;
            logEntity.status = (pojo.status != null) ? pojo.status : ConstantsInUse.ACTIVE_STATUS;

            cassandraLogDao.save(logEntity);
            return ConstantsInUse.SUCCESS_MESSAGE;
        } catch (Exception e) {
            e.printStackTrace();
            return ConstantsInUse.FAILURE_MESSAGE;
        }
    }

    public List<CassandraLogEntity> findAllLogs() {
        return cassandraLogDao.findAll();
    }

    public Optional<CassandraLogEntity> findLogById(UUID id) {
        return cassandraLogDao.findById(id);
    }

    public List<CassandraLogEntity> findLogsByIp(String ip) {
        return cassandraLogDao.findByIp(ip);
    }

    public List<CassandraLogEntity> findLogsByThreatType(String threatType) {
        return cassandraLogDao.findByThreatType(threatType);
    }

    public List<CassandraLogEntity> findLogsByAction(String action) {
        return cassandraLogDao.findByAction(action);
    }

    public String deleteLogsById(List<UUID> ids) {
        try {
            for (UUID id : ids) {
                Optional<CassandraLogEntity> optionalLog = cassandraLogDao.findById(id);
                if (optionalLog.isPresent()) {
                    CassandraLogEntity log = optionalLog.get();
                    log.status = ConstantsInUse.INACTIVE_STATUS;
                    cassandraLogDao.save(log);
                }
            }
            return ConstantsInUse.SUCCESS_MESSAGE;
        } catch (Exception e) {
            e.printStackTrace();
            return ConstantsInUse.FAILURE_MESSAGE;
        }
    }

    public String hardDeleteLogs(List<UUID> ids) {
        try {
            for (UUID id : ids) {
                cassandraLogDao.deleteById(id);
            }
            return ConstantsInUse.SUCCESS_MESSAGE;
        } catch (Exception e) {
            e.printStackTrace();
            return ConstantsInUse.FAILURE_MESSAGE;
        }
    }
}
