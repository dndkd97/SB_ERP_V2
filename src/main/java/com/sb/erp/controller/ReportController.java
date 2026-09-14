package com.sb.erp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sb.erp.api.ReportApi;
import com.sb.erp.dto.MyWeeklyReportDto;
import com.sb.erp.security.CustomUserDetails;
import com.sb.erp.service.TaskService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/report")
public class ReportController {
	
    @Autowired private TaskService taskService;
    @Autowired private ReportApi reportApi;

	@GetMapping("/my-weekly-report") //개인보고서 pdf
    public ResponseEntity<byte[]> myReport(Authentication auth){
    	CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
    	int empId = user.getUser().getEmpId();
    	MyWeeklyReportDto dto = taskService.myWeeklyReport(empId);
        
    	//태스크 없으면 나가주세요
        if(dto == null || dto.getTotalTask() == 0) { return ResponseEntity.badRequest().build(); }
        dto.setDelayedTaskNames( taskService.delayedTaskNames(empId) );
        
        byte[] pdf = reportApi.createMyWeeklyReport(dto);
        return ResponseEntity.ok() .contentType(MediaType.APPLICATION_PDF)
        						   .header("Content-Disposition", "attachment; filename=\"MyWeeklyReport.pdf\"")
        						   .body(pdf);
    }
    
    @GetMapping("/my-weekly-report/check") //태스크 존재여부 확인
    @ResponseBody
    public ResponseEntity<Boolean> checkAvailable(Authentication auth) {
    	CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
    	int empId = user.getUser().getEmpId();
        MyWeeklyReportDto dto = taskService.myWeeklyReport(empId);
        boolean available = (dto != null && dto.getTotalTask() > 0);
        return ResponseEntity.ok(available);
    }
}
