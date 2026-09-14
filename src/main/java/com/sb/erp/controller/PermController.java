package com.sb.erp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sb.erp.dto.AuthPermDto;
import com.sb.erp.dto.EmpAuthDto;
import com.sb.erp.service.AuthService;
import com.sb.erp.service.EmpService;

@Controller
public class PermController {

	@Autowired AuthService authService;
	@Autowired EmpService empService;

	// ─── 권한 관리 ─────────────────

	// 권한 목록 (부여 사원 수 포함)
	@GetMapping("/perm/list")
	public String list(@RequestParam(required = false) Integer autId, Model model) {
	    // 좌측: 권한 목록 (사원 수 포함)
	    List<AuthPermDto> authList = authService.selectAll();
	    model.addAttribute("authList", authList);

	    // 우측: 선택된 권한이 있으면 상세 + 부여 사원 목록
	    if (autId != null) {
	        AuthPermDto selectedRole = authService.selectOneById(autId);
	        if (selectedRole != null) {
	            model.addAttribute("selectedRole", selectedRole);
	            model.addAttribute("empList", authService.selectEmpsByAuthId(autId));
	        }
	    }

	    return "perm/list";
	}

	// 권한 등록 폼
	@GetMapping("/perm/add")
	public String addForm() {
		return "perm/add";
	}

	@PostMapping("/perm/add")
	public String addProcess(AuthPermDto dto) {
		authService.insert(dto);
		return "redirect:/perm/list";
	}

	// 권한 수정 폼
	@GetMapping("/perm/edit")
	public String editForm(@RequestParam int autId, Model model) {
		model.addAttribute("auth", authService.selectOneById(autId));
		return "perm/edit";
	}

	@PostMapping("/perm/edit")
	public String editProcess(AuthPermDto dto) {
		authService.update(dto);
		return "redirect:/perm/list";
	}

	// 권한 삭제
	@PostMapping("/perm/delete")
	public String delete(@RequestParam int autId) {
		authService.delete(autId);
		return "redirect:/perm/list";
	}

	
	
	// ─── 사원-권한 매핑 ────────────
	

	// 사원별 권한 관리 화면 (사원의 권한 목록)
	@GetMapping("/perm/emp")
	public String empAuthList(@RequestParam int empId, Model model) {
		model.addAttribute("emp", empService.selectByEmpId(empId));
		model.addAttribute("authList", authService.selectAuthsByEmpId(empId));
		model.addAttribute("allAuths", authService.selectAll());
		return "perm/emp";
	}

	// 권한 부여
	@PostMapping("/perm/grant")
	public String grant(EmpAuthDto dto) {
		authService.grantAuth(dto);
		return "redirect:/perm/emp?empId=" + dto.getEmpId();
	}

	// 권한 회수
	@PostMapping("/perm/revoke")
	public String revoke(EmpAuthDto dto) {
		authService.revokeAuth(dto);
		return "redirect:/perm/emp?empId=" + dto.getEmpId();
	}
}