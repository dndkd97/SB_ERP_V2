package com.sb.erp.controller;

import java.time.LocalDate;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.sb.erp.dto.EmpDto;
import com.sb.erp.security.CustomUserDetails;

import jakarta.servlet.http.HttpSession;

@Controller
public class BasicController {
	
	@GetMapping("/")
	public String index(Authentication auth, HttpSession session, Model model) { 
		
		// 403 오류시 - 메시지 표시 후 제거
	    Object msg = session.getAttribute("accessDeniedMsg");
	    if (msg != null) {
	        model.addAttribute("accessDeniedMsg", msg);
	        session.removeAttribute("accessDeniedMsg");
	    }
	    
	    // 현재 로그인된 사용자 정보
	    CustomUserDetails authUser = (CustomUserDetails) auth.getPrincipal();
	    EmpDto user = authUser.getUser();
	    
	    model.addAttribute("user", user);
	    model.addAttribute("today", LocalDate.now());
	   
		return "dashboard/index"; 
		// return "dashboard/exec"; // 임원 
  		// return "dashboard/sysadmin"; // 시스템관리자 
  		// return "dashboard/admin"; // 관리자
	}
	
}
