package com.sb.erp.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sb.erp.dto.CompanyDto;
import com.sb.erp.dto.DeptDto;
import com.sb.erp.dto.DeptSearchDto;
import com.sb.erp.dto.StatsDeptDto;
import com.sb.erp.security.CustomUserDetails;
import com.sb.erp.service.CompanyService;
import com.sb.erp.service.DeptService;
import com.sb.erp.service.EmpService;

@Controller
@RequestMapping("/dept")
public class DeptController {
	@Autowired DeptService service;
	@Autowired CompanyService comService;
	@Autowired EmpService empService;
	
	// 부서 목록 
	@GetMapping("/list")
	public String orgTree(Integer comId, Model model, Authentication auth) {
		CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
		// 권한 문자열만 추출해서 비교
	    Set<String> authNames = user.getAuthorities().stream()
	            .map(GrantedAuthority::getAuthority)
	            .collect(Collectors.toSet());

	    boolean isAdmin = authNames.contains("ROOT") || authNames.contains("ROLE_ADMIN");

	    if (!isAdmin || comId == null) {
	        comId = user.getUser().getComId();
	    }
		
		CompanyDto com = comService.selectOneById(comId);
		// 통계데이터
		StatsDeptDto stats = service.selecStats(comId);
		
		model.addAttribute("stats", stats);
		model.addAttribute("com", com);
		model.addAttribute("items", service.selectOrgTree(comId));
		return "/dept/list";
	}
	
	// 부서 등록 폼
	@GetMapping("/add")
	public String addForm(@RequestParam("comId") int comId, Model model) {
		CompanyDto com = comService.selectOneById(comId);
		model.addAttribute("deptList", service.selectOrgTree(comId));
		model.addAttribute("com", com);
		return "/dept/add";
	}
	
	// 부서 등록 기능
	@PostMapping("/add")
	public String addForm_post(@RequestParam("comId") int comId, DeptDto dto, RedirectAttributes rttr) {
		String msg = "부서 등록에 실패하였습니다.";
		if(service.insert(dto) > 0) { msg = "부서등록에 성공하였습니다."; }
		rttr.addFlashAttribute("msg", msg);
		return "redirect:/dept/list?comId="+comId;
	}
	
	// 부서 수정 폼
	@GetMapping("/edit")
	public String editForm(
			@RequestParam("deptId") int deptId, 
			@RequestParam("comId") int comId, 
			Model model) {
		CompanyDto com = comService.selectOneById(comId);
		DeptDto dept = service.selectOneById(deptId);
		
		model.addAttribute("dept", dept);
		model.addAttribute("deptList", service.selectOrgTree(comId));
		model.addAttribute("com", com);
		model.addAttribute("deptEmpList", empService.selectByDeptId(deptId));
		return "/dept/edit";
	}
	
	// 부서 수정 기능
	@PostMapping("/edit")
	public String editForm_post(int deptId, int comId, DeptDto dto, RedirectAttributes rttr) {
		DeptDto se = service.selectOneById(deptId);
		try {
			service.update(dto);
			rttr.addFlashAttribute("msg", "부서 수정 성공.");
		} catch (IllegalStateException e) {
			rttr.addFlashAttribute("msg", e.getMessage());
		}
		return "redirect:/dept/list?comId="+se.getComId();
	}
	
	//부서 삭제 기능
	@PostMapping("/delete")
	public String delete_post(int deptId, RedirectAttributes rttr) {
		DeptDto dto = service.selectOneById(deptId);
	    int empCount = service.countEmployees(deptId);
		try {
			// 사원 정보 0명이면, 진짜 부서삭제
			if(empCount == 0) {
				service.delete(deptId);
				rttr.addFlashAttribute("msg", "부서 삭제 성공.");
				return "redirect:/dept/list?comId=" + dto.getComId();
			}
		} catch (IllegalStateException e) {
			rttr.addFlashAttribute("msg", e.getMessage());
		}
		//소속사원이 있는 경우: 즉시 삭제 대신 PENDING_DELETE로 전환
		service.softDelete(deptId);
		rttr.addFlashAttribute("msg", "사원이 존재해 사원 부서이관 페이지로 이동합니다.");
	    return "redirect:/dept/transfer/list?deptId=" + deptId; 
	}
	
	// 부서 상세 조회
	@GetMapping("/detail")
	public String detail(@RequestParam(value="deptId", required = false) Integer deptId, Model model, Authentication auth) {
		CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
		if(deptId == null) {
			deptId = user.getUser().getDeptId();
		}
		
		//계층 경로
		model.addAttribute("ancestorChain", service.getAncestorChain(deptId));
		model.addAttribute("dept", service.selectOneById(deptId));
		model.addAttribute("deptEmpList", empService.selectByDeptId(deptId));
		return "/dept/detail";
	}
	
	// 부서 코드 중복 체크 (ajax)
	@GetMapping("/checkDeptCode")
	@ResponseBody
	public Map<String, Object> checkDeptCode(DeptSearchDto search){
		Map<String, Object> res = new HashMap<>();
		DeptDto dto = service.isDuplicateDeptCode(search);
		
		if(dto != null) res.put("duplicate", true);
		else res.put("duplicate", false);
		
		return res;
	}
}
