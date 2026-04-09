package com.example.firewall.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.firewall.dto.RuleMasterPojo;
import com.example.firewall.service.RuleMasterService;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/rule")
public class RuleController {

    @Autowired
    private RuleMasterService ruleMasterService;

    @PostMapping("/ruleInsert")
    public String ruleInsert(@RequestBody RuleMasterPojo ruleMasterPojo) {
        
        return ruleMasterService.ruleInsert(ruleMasterPojo);

    }

    @GetMapping("/getAllRules")
    public ResponseEntity<?> getAllRules() {

        return ResponseEntity.ok(ruleMasterService.findAllRule());

    }

    @PostMapping("/delete")
    public String deleteRule(@RequestBody List<RuleMasterPojo> rulePojo) {

        return ruleMasterService.deleteRuleById(rulePojo);

    }
    
}