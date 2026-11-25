package com.example.firewall.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.firewall.dto.RuleMasterPojo;
import com.example.firewall.entity.RuleMasterEntity;
import com.example.firewall.repository.RuleMasterDao;
import com.example.firewall.utils.ConstantsInUse;

@Service
public class RuleMasterService {

    @Autowired
    private RuleMasterDao ruleMasterDao;

    @Autowired
    private  ThreatTypeMasterService threatTypeMasterService;

    @Autowired
    private AuthService authService;

    public String ruleInsert(RuleMasterPojo rule){
        String effectivePattern = (rule.pattern != null && !rule.pattern.isBlank())
                ? rule.pattern
                : rule.rulePattern;
        if (effectivePattern == null || effectivePattern.isBlank()) {
            return "pattern is required";
        }

        RuleMasterEntity rules = ruleMasterDao.findRule(effectivePattern, ConstantsInUse.ACTIVE_STATUS);
        if(rules != null){
            return ConstantsInUse.DUPLICATE_MESSAGE;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        RuleMasterEntity ruleEntity = new RuleMasterEntity();
        ruleEntity.ruleName = (rule.ruleName != null && !rule.ruleName.isBlank()) ? rule.ruleName : effectivePattern;
        ruleEntity.pattern = effectivePattern;
        ruleEntity.pattern_type = (rule.pattern_type != null && !rule.pattern_type.isBlank()) ? rule.pattern_type : "REGEX";
        ruleEntity.field = (rule.field != null && !rule.field.isBlank()) ? rule.field : "path";
        ruleEntity.action = (rule.action != null && !rule.action.isBlank()) ? rule.action.toUpperCase() : ConstantsInUse.BLOCK_THE_REQUEST;
        ruleEntity.status = (rule.status != null && !rule.status.isBlank()) ? rule.status.toLowerCase() : ConstantsInUse.ACTIVE_STATUS;
        if (rule.threatId != null) {
            ruleEntity.threat = threatTypeMasterService.findById(rule.threatId);
        }
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            ruleEntity.user = authService.getByUserName(auth.getName());
        }

        RuleMasterEntity ruleExcel = ruleMasterDao.saveAndFlush(ruleEntity);

        try (FileInputStream file = new FileInputStream(ConstantsInUse.RULEFILEPATH);
            Workbook workbook = WorkbookFactory.create(file)){
                
                Sheet sheet = workbook.getSheetAt(0);
                int lastRow = sheet.getLastRowNum();

                String threatType = ruleExcel.threat != null ? ruleExcel.threat.threat_type
                        : (rule.threat_type != null ? rule.threat_type : "CUSTOM_RULE");
                String priority = ruleExcel.threat != null ? ruleExcel.threat.priority
                        : (rule.priority != null ? rule.priority : "HIGH");

                Row newRow = sheet.createRow(lastRow + 1);
                newRow.createCell(0).setCellValue(threatType);
                newRow.createCell(1).setCellValue(ruleExcel.pattern);
                newRow.createCell(2).setCellValue(ruleExcel.pattern_type);
                newRow.createCell(3).setCellValue(ruleExcel.field);
                newRow.createCell(4).setCellValue(priority);

                try (FileOutputStream out = new FileOutputStream(ConstantsInUse.RULEFILEPATH)) {
                    workbook.write(out);
                    System.out.println("Data appended successfully.");
                }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ConstantsInUse.SUCCESS_MESSAGE;

    }

    public String ruleUpdate(RuleMasterPojo rule) {
        Long targetId = rule.ruleId != null ? rule.ruleId : rule.id;
        if (targetId == null) {
            return "ruleId is required";
        }
        RuleMasterEntity existing = ruleMasterDao.findById(targetId).orElse(null);
        if (existing == null) {
            return "rule not found";
        }
        if (rule.ruleName != null && !rule.ruleName.isBlank()) {
            existing.ruleName = rule.ruleName;
        }
        String effectivePattern = (rule.pattern != null && !rule.pattern.isBlank()) ? rule.pattern : rule.rulePattern;
        if (effectivePattern != null && !effectivePattern.isBlank()) {
            existing.pattern = effectivePattern;
        }
        if (rule.field != null && !rule.field.isBlank()) {
            existing.field = rule.field;
        }
        if (rule.action != null && !rule.action.isBlank()) {
            existing.action = rule.action.toUpperCase();
        }
        if (rule.status != null && !rule.status.isBlank()) {
            existing.status = rule.status.toLowerCase();
        }
        ruleMasterDao.saveAndFlush(existing);
        return ConstantsInUse.SUCCESS_MESSAGE;
    }

    public List<RuleMasterEntity> findAllRule(){

        List<RuleMasterEntity> allRules = ruleMasterDao.findAllVisibleRules();
        return allRules != null ? allRules : List.of();

    }

    public String deleteRuleById(List<RuleMasterPojo> ruleIds){

        for(RuleMasterPojo rule:ruleIds){
            RuleMasterEntity rules = null;
            Long targetId = rule.ruleId != null ? rule.ruleId : rule.id;
            if (targetId != null) {
                rules = ruleMasterDao.findById(targetId).orElse(null);
            }
            String effectivePattern = (rule.pattern != null && !rule.pattern.isBlank())
                    ? rule.pattern
                    : rule.rulePattern;
            if (rules == null && effectivePattern != null) {
                rules = ruleMasterDao.findRule(effectivePattern, ConstantsInUse.ACTIVE_STATUS);
            }
            if(rules != null){
                rules.status = "deleted";
                ruleMasterDao.saveAndFlush(rules);

                try (FileInputStream file = new FileInputStream(ConstantsInUse.RULEFILEPATH);
                    Workbook workbook = WorkbookFactory.create(file)){

                    Sheet sheet = workbook.getSheetAt(0);
                    DataFormatter formatter = new DataFormatter();
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

                    for(Row row:sheet){

                        List<String> rowData = new ArrayList<>();
                        for (Cell cell : row) {

                            String cellValue = formatter.formatCellValue(cell, evaluator); 
                            rowData.add(cellValue);

                            if (rules.pattern != null && cellValue.contains(rules.pattern)) {
                                Row rowToremove = sheet.getRow(cell.getRowIndex());
                                if (rowToremove != null) {
                                    sheet.removeRow(rowToremove);
                                }
                            }
                        }
                    }
        
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        return ConstantsInUse.SUCCESS_MESSAGE;
    }
    
}