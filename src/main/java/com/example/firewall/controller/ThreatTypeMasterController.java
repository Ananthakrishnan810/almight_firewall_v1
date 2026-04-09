package com.example.firewall.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.firewall.dto.ThreatTypeMasterPojo;
import com.example.firewall.service.ThreatTypeMasterService;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("threat")
public class ThreatTypeMasterController {
 

    @Autowired
    private ThreatTypeMasterService threatTypeMasterService;

    @PostMapping("/threatInsert")
    public String threatAddition(@RequestBody ThreatTypeMasterPojo threatTypeMasterPojo) {
        
        return threatTypeMasterService.insertThreat(threatTypeMasterPojo);
        
    }

    @GetMapping("/getAllThreat")
    public ResponseEntity<?> getAllThreat() {

        return ResponseEntity.ok(threatTypeMasterService.findAllThreat());

    }

    @PostMapping("/delete")
    public String deleteThreat(@RequestBody List<ThreatTypeMasterPojo> threatPojo) {

        return threatTypeMasterService.deleteThreatById(threatPojo);

    }
    
}