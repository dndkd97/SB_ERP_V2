package com.sb.erp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sb.erp.dto.EmpDto;
import com.sb.erp.dto.EmpSearchDto;
import com.sb.erp.service.DeptService;
import com.sb.erp.service.EmpService;
import com.sb.erp.service.PosService;
import com.sb.erp.util.PagingUtil;
import com.sb.erp.util.SecurityUtil;
import com.sb.erp.dto.EvalReportDto;
import com.sb.erp.service.EvalReportService;

@Controller
public class EmpController {

	@Autowired EmpService empService;
	@Autowired PosService posService;
	@Autowired DeptService deptService;
	@Autowired PasswordEncoder passEncoder;
	@Autowired EvalReportService evalReportService;
	
	
	// ─── 목록 조회 (검색 + 페이징) ────────────────────
	@GetMapping("/emp/list")
	public String list(EmpSearchDto search, 
			@RequestParam(value = "searched", required = false) String searched,
			Model model) {
		
		boolean hasFilter = search.getDeptId() != null || search.getPosId() != null
				|| (search.getEmpStatus() != null && !search.getEmpStatus().isEmpty())
				|| (search.getKeyword() != null && !search.getKeyword().trim().isEmpty());

		if (searched != null && hasFilter) {
			// 현재 페이지 보정
			int currentPage = (search.getPage() == null || search.getPage() < 1) ? 1 : search.getPage();

			// 전체 건수 → 페이징 계산
			int total = empService.selectCnt(search);
			PagingUtil paging = new PagingUtil(total, currentPage);

			// 계산된 페이징 정보를 search에 세팅 후 조회
			search.setPstartno(paging.getPstartno());
			search.setOnepagelist(paging.getOnepagelist());
			List<EmpDto> empList = empService.search(search);

			model.addAttribute("empList", empList);
			model.addAttribute("paging", paging);
		}

		model.addAttribute("search", search);
		model.addAttribute("posList", posService.selectAll());
		model.addAttribute("deptList", deptService.selectAll());
		model.addAttribute("loginEmpId", SecurityUtil.getCurrentEmpId());
		model.addAttribute("isAdmin", SecurityUtil.isAdmin());
		return "emp/list";
	}
	
	
	// ─── 상세 조회 ──────────────────────────────────
	// ─── 상세 조회 ──────────────────────────────────
	@GetMapping("/emp/detail")
	public String detail(@RequestParam int empId, Model model) {
	    int loginEmpId = SecurityUtil.getCurrentEmpId();
	    boolean isAdmin = SecurityUtil.isAdmin();
	    
	    // 본인 혹은 관리자 여부 확인
	    if (empId != loginEmpId && !isAdmin) {
	        return "redirect:/emp/detail?empId=" + loginEmpId;
	    }
	    
	    EmpDto emp = empService.selectByEmpId(empId);
	    model.addAttribute("emp", emp);

	    // 최근 AI 리포트 임베드 (관리자/본인 모두 노출)
	    // - 리포트가 아직 없는 사원이면 null → 템플릿에서 th:if로 카드 자체를 안 그림
	    EvalReportDto latestReport = evalReportService.selectLatestByEmpId(empId);
	    model.addAttribute("latestReport", latestReport);

	    // 본인 여부 판별용 loginEmpId 전달
	    model.addAttribute("loginEmpId", loginEmpId);
	    model.addAttribute("isAdmin", isAdmin);
	    return "emp/detail";
	}
	
	
	// ─── 사원 등록 ──────────────────────────────────
	@GetMapping("/emp/add")
	public String addForm(Model model) {
		model.addAttribute("posList", posService.selectAll());
		model.addAttribute("deptList", deptService.selectAll());
		return "emp/add";
	}

	@PostMapping("/emp/add")
	public String addProcess(EmpDto dto) {
		empService.insert(dto);
		return "redirect:/emp/list";
	}
	
	
	// ─── 사원 정보 수정 ─────────────────────────────
	@GetMapping("/emp/edit")
	public String editForm(@RequestParam int empId, Model model) {
		int loginEmpId = SecurityUtil.getCurrentEmpId();
		boolean isAdmin = SecurityUtil.isAdmin();

		// 본인이 아니고 관리자도 아니면 본인 수정 화면으로
		if (empId != loginEmpId && !isAdmin) {
			return "redirect:/emp/edit?empId=" + loginEmpId;
		}
		
		model.addAttribute("emp", empService.selectByEmpId(empId));
		model.addAttribute("posList", posService.selectAll());
		model.addAttribute("deptList", deptService.selectAll());
		model.addAttribute("isAdmin", isAdmin);
		return "emp/edit";
	}

	@PostMapping("/emp/edit")
	public String editProcess(EmpDto dto) {
		int loginEmpId = SecurityUtil.getCurrentEmpId();
		boolean isAdmin = SecurityUtil.isAdmin();

		// 타인 정보 수정시도 차단
		if (dto.getEmpId() != loginEmpId && !isAdmin) {
			return "redirect:/emp/detail?empId=" + loginEmpId;
		}
		
		// 본인 수정 시 관리자만 수정 가능한 필드는 무시 (덮어쓰기 방지)
		if (!isAdmin) {
			// 현재 저장된 값으로 보정
			EmpDto current = empService.selectByEmpId(dto.getEmpId());
			dto.setEmpName(current.getEmpName());
			dto.setDeptId(current.getDeptId());
			dto.setPosId(current.getPosId());
			dto.setEmpStatus(current.getEmpStatus());
		}
		
		empService.update(dto);
		return "redirect:/emp/detail?empId=" + dto.getEmpId();
	}
	
	
	// ─── 비밀번호 초기화 (관리자) ───────────────────
	@GetMapping("/emp/resetPass")
	public String resetPassForm(@RequestParam int empId, Model model) {
		model.addAttribute("emp", empService.selectByEmpId(empId));
		return "emp/resetPass";
	}

	@PostMapping("/emp/resetPass")
	public String resetPassProcess(@RequestParam int empId) {
		empService.resetPassByEmpNo(empId);
		return "redirect:/emp/detail?empId=" + empId;
	}
	
	
	
	// ─── 비밀번호 변경 (본인) ────────────────────────
	@GetMapping("/emp/editPass")
	public String editPassForm(@RequestParam int empId, Model model) {
		int loginEmpId = SecurityUtil.getCurrentEmpId();

	    // 본인만 접근 가능 (관리자는 resetPass로)
	    if (empId != loginEmpId) {
	        return "redirect:/emp/detail?empId=" + loginEmpId;
	    }
	    
		model.addAttribute("emp", empService.selectByEmpId(empId));
		return "emp/editPass";
	}

	@PostMapping("/emp/editPass")
	public String editPassProcess(@RequestParam int empId, 
			@RequestParam String currentPass,
			@RequestParam String editPass, 
			@RequestParam String checkPass) {
		
		int loginEmpId = SecurityUtil.getCurrentEmpId();
	   
		// 본인만 (URL 조작 방어)
	    if (empId != loginEmpId) {
	        return "redirect:/emp/detail?empId=" + loginEmpId;
	    }
		
		// 새 비밀번호 일치 확인
		if (!editPass.equals(checkPass)) {
			return "redirect:/emp/editPass?empId=" + empId;
		}

		// 현재 비번 검증 + 새 비번 저장 (Service가 통합 처리)
		int result = empService.changePassword(empId, currentPass, editPass);
		if (result != 1) {
			return "redirect:/emp/editPass?empId=" + empId;
		}

		return "redirect:/emp/detail?empId=" + empId;
	}
	
	
	
	// ─── 중복 검사 ────────────────────────────
	@GetMapping("/emp/checkEmail")
	@ResponseBody
	public Map<String, Object> checkEmail(@RequestParam String empEmail) {
		Map<String, Object> result = new HashMap<>();
		result.put("duplicate", empService.isEmailDuplicate(empEmail));
		return result;
	}

	@GetMapping("/emp/checkMobile")
	@ResponseBody
	public Map<String, Object> checkMobile(@RequestParam String empMobile) {
		Map<String, Object> result = new HashMap<>();
		result.put("duplicate", empService.isMobileDuplicate(empMobile));
		return result;
	}

	@GetMapping("/emp/checkEmpNo")
	@ResponseBody
	public Map<String, Object> checkEmpNo(@RequestParam String empNo) {
		Map<String, Object> result = new HashMap<>();
		result.put("duplicate", empService.isEmpNoDuplicate(empNo));
		return result;
	}
}