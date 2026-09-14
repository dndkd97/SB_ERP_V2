package com.sb.erp.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sb.erp.dto.EmpDto;
import com.sb.erp.service.AuthService;
import com.sb.erp.service.CompanyService;
import com.sb.erp.service.EmpService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/auth")
public class AuthController {
	@Autowired AuthService service;
	@Autowired EmpService empService;
	@Autowired CompanyService comService;
	@Autowired PasswordEncoder passEncoder;
	
	// 로그인 페이지로 이동
	@GetMapping("/login")
	public String login(Authentication auth) {
	    // 이미 로그인된 사용자면 로그인 페이지 접근 차단 → 메인으로 리다이렉트
	    if (auth != null && auth.isAuthenticated()) {
	        return "redirect:/"; // 실제 메인/대시보드 경로에 맞게 수정
	    }
		return "/auth/login";
	}
	
	// 비밀번호를 변경하려는 사용자가 실제로 존재하는지 여부 확인
	@PostMapping("/confirm")
	@ResponseBody
	public Map<String, Object> confirm(EmpDto dto, HttpSession session) {
		Map<String, Object> result = new HashMap<>();
		EmpDto emp = empService.selectForVerify(dto);
		if (emp == null) {
	        result.put("state", "FAIL");
	        return result;
	    }
		
		session.setAttribute("empId", emp.getEmpId());
		result.put("state", "OK");
		return result;
	}
	
	// 로그인 후 강제 재설정
	@GetMapping("/resetPass")
	public String reset_pass(Authentication authentication, Model model) {
	    if (authentication == null) return "redirect:/auth/login";
	    
	    EmpDto emp = empService.selectByEmpEmail(authentication.getName());
		model.addAttribute("emp", emp);
		return "/auth/resetPass";
	}
	
	// 비밀번호 분실 - session(empId) 기반, 본인확인 후에만 진입 가능
	@GetMapping("/forgotResetPass")
	public String reset_pass(HttpSession session, Model model) {
		Integer empId = (Integer) session.getAttribute("empId");
		if(empId == null || empId == 0) return "redirect:/auth/login";
		model.addAttribute("emp", empService.selectAuthByEmpId(empId));
		return "/auth/resetPass";
	}
	
	// 비밀번호 재설정 기능
	@PostMapping("/updatePass")
	public String update_pass(@RequestParam(value="empPass", required = true) String empPass,
							  HttpSession session
			) {
		Integer empId = (Integer) session.getAttribute("empId");
		if (empId == null) {
			return "redirect:/auth/login";
		}
		
		EmpDto dto = new EmpDto();
		dto.setEmpPass(passEncoder.encode(empPass)); dto.setEmpId(empId);
		empService.updatePassByEmpIdOnly(dto);
		
		// 1회용 처리 — 재사용 방지
		session.removeAttribute("empId"); 
		return "redirect:/auth/login";
	}
	
	// 로그인 실패시 화면 구현
	@GetMapping("/fail")
	public String loginFail(HttpServletRequest request) {

	    // TODO: 실패 로그 적재 — 마지막 시도 이메일은 세션에서 꺼낼 수 있습니다.
	    // String username = (String) request.getSession()
	    //         .getAttribute("SPRING_SECURITY_LAST_USERNAME");
	    // String ip = request.getRemoteAddr();
	    // failLogService.save(username, ip, new Date());

	    return "redirect:/auth/login?error=true";
	}
}
