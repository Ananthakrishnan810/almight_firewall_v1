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

        RuleMasterEntity rules = ruleMasterDao.findRule(rule.pattern, ConstantsInUse.ACTIVE_STATUS);
        if(rules != null){
            return ConstantsInUse.DUPLICATE_MESSAGE;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        RuleMasterEntity ruleEntity = new RuleMasterEntity();
        ruleEntity.pattern = rule.pattern;
        ruleEntity.pattern_type = rule.pattern_type;
        ruleEntity.field = rule.field;
        ruleEntity.threat = threatTypeMasterService.findById(rule.threatId);
        ruleEntity.user = authService.getByUserName(auth.getName());

        RuleMasterEntity ruleExcel = ruleMasterDao.saveAndFlush(ruleEntity);

        try (FileInputStream file = new FileInputStream(ConstantsInUse.RULEFILEPATH);
            Workbook workbook = WorkbookFactory.create(file)){
                
                Sheet sheet = workbook.getSheetAt(0);
                int lastRow = sheet.getLastRowNum();

                 Row newRow = sheet.createRow(lastRow + 1);
                newRow.createCell(0).setCellValue(ruleExcel.threat.threat_type);
                newRow.createCell(1).setCellValue(ruleExcel.pattern);
                newRow.createCell(2).setCellValue(ruleExcel.pattern_type);
                newRow.createCell(3).setCellValue(ruleExcel.field);
                newRow.createCell(4).setCellValue(ruleExcel.threat.priority);

                try (FileOutputStream out = new FileOutputStream(ConstantsInUse.RULEFILEPATH)) {
                    workbook.write(out);
                    System.out.println("Data appended successfully.");
                }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ConstantsInUse.SUCCESS_MESSAGE;

    }

    public RuleMasterEntity findAllRule(){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ruleMasterDao.findAllRule(auth.getName(),ConstantsInUse.ACTIVE_STATUS);

    }

    public String deleteRuleById(List<RuleMasterPojo> ruleIds){

        for(RuleMasterPojo rule:ruleIds){
            RuleMasterEntity rules = ruleMasterDao.findRule(rule.pattern,ConstantsInUse.ACTIVE_STATUS);
            if(rules != null){
                rules.status = ConstantsInUse.INACTIVE_STATUS;
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

                            if (cellValue.contains(rule.pattern)) {
                                Row rowToremove = sheet.getRow(cell.getRowIndex());
                                sheet.removeRow(rowToremove);
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