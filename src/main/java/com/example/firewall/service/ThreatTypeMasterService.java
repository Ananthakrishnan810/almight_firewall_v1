package com.example.firewall.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.firewall.dto.ThreatTypeMasterPojo;
import com.example.firewall.entity.ThreatTypeMasterEntity;
import com.example.firewall.repository.ThreatTypeMasterDao;
import com.example.firewall.utils.ConstantsInUse;

@Service
public class ThreatTypeMasterService {

    @Autowired
    private ThreatTypeMasterDao threatTypeMasterDao;

    public String insertThreat(ThreatTypeMasterPojo threat){

        ThreatTypeMasterEntity threatEntity = threatTypeMasterDao.findThreat(threat.threat_type, threat.priority,ConstantsInUse.ACTIVE_STATUS);
        if(threatEntity != null){
            return ConstantsInUse.DUPLICATE_MESSAGE;
        }

        ThreatTypeMasterEntity threatType = new ThreatTypeMasterEntity();
        threatType.threat_type = threat.threat_type;
        threatType.priority = threat.priority;

        threatTypeMasterDao.saveAndFlush(threatType);

        return ConstantsInUse.SUCCESS_MESSAGE;

    }

    public ThreatTypeMasterEntity findById(Long threatId){

        return threatTypeMasterDao.findThreatById(threatId,ConstantsInUse.ACTIVE_STATUS);

    }

    public ThreatTypeMasterEntity findAllThreat(){

        return threatTypeMasterDao.findAllThreat(ConstantsInUse.ACTIVE_STATUS);

    }

    public String deleteThreatById(List<ThreatTypeMasterPojo> threadtIds){

        for(ThreatTypeMasterPojo threat:threadtIds){
            ThreatTypeMasterEntity threatype = threatTypeMasterDao.findThreat(threat.threat_type,threat.priority,ConstantsInUse.ACTIVE_STATUS);
            if(threatype != null){
                threatype.status = ConstantsInUse.INACTIVE_STATUS;
                threatTypeMasterDao.saveAndFlush(threatype);
            }
        }

        return ConstantsInUse.SUCCESS_MESSAGE;
    }
    
}