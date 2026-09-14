package com.sb.erp.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sb.erp.dto.AuthPermDto;
import com.sb.erp.dto.ComSearchDto;
import com.sb.erp.dto.CompanyDto;
import com.sb.erp.dto.DeptDto;
import com.sb.erp.dto.EmpDto;
import com.sb.erp.dto.StatsComDto;
import com.sb.erp.dto.StatsDeptDto;
import com.sb.erp.exception.FileUploadException;
import com.sb.erp.security.CustomUserDetails;
import com.sb.erp.service.CompanyService;
import com.sb.erp.service.DeptService;
import com.sb.erp.service.EmpService;
import com.sb.erp.service.PermService;
import com.sb.erp.util.FileUploadDto;
import com.sb.erp.util.FileUploadType;
import com.sb.erp.util.FileUploadUtil;
import com.sb.erp.util.PagingUtil;
import com.sb.erp.util.SecurityUtil;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/com")
public class CompanyController {
	@Autowired CompanyService service;
	@Autowired EmpService empService;
	@Autowired PermService permService;
	@Autowired DeptService deptService;
	
	// 회사 등록
	@GetMapping("/add")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('ROOT')")
	public String addForm() {
		return "/com/form";
	}
	
	//회사 등록 기능
	@PostMapping("/add")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('ROOT')")
	public String add(CompanyDto dto, 
			@RequestParam(value="logoFile", required=false) MultipartFile logoFile,
			RedirectAttributes rttr) {
		String msg = "회사 등록에 실패하였습니다.";
		
		try {
			if (logoFile != null && !logoFile.isEmpty()) {
				FileUploadDto result = FileUploadUtil.upload(logoFile, FileUploadType.COMPANY_LOGO);
				dto.setComLogo(result.getFileUrl());
			}
			if(service.add(dto) > 0) { msg = "회사 등록에 성공하셨습니다."; }
		} catch (FileUploadException e) {
			msg = e.getMessage();
		}
		
		rttr.addFlashAttribute("msg", msg);
	    return "redirect:/com/list";
	}
	
	// 회사 사업자 번호 중복 체크 (ajax)
	@GetMapping("/checkBizNo")
	@ResponseBody
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('ROOT')")
	public Map<String, Object> checkBizNo(String bizNo){
		Map<String, Object> res = new HashMap<>();
		CompanyDto dto = service.isDuplicateBizNo(bizNo);
		
		if(dto != null) res.put("duplicate", true);
		else res.put("duplicate", false);
		
		return res;
	}
	
	// 회사 목록 조회
	@GetMapping("/list")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('ROOT')")
	public String list(ComSearchDto search, Model model, Authentication auth) {
		if (!SecurityUtil.isAdminOrRoot(auth)) {
	        return "redirect:/com/my";
	    }
		
		int listtotal = service.listTotal(search);
		// 검색 조건이 null
		boolean isEmpty = !search.hasSearchCondition();
		
		List<CompanyDto> list = new ArrayList<>();
		PagingUtil paging;
		
		if(isEmpty) {
	    	paging = new PagingUtil(0, search.getPstartno());
		}else {
			paging = new PagingUtil(listtotal, search.getPstartno(), search.getOnepagelist(), 10);
			list = service.list(search);
		}
		
		//통계 데이터
		StatsComDto stats = service.selectStats();
		model.addAttribute("stats", stats);
		model.addAttribute("paging", paging);
		model.addAttribute("items", list);
		return "/com/list";
	}
	
	// 검색 조회 목록 상위 5개 (ajax)
	@GetMapping("/suggest")
	@ResponseBody
	public List<CompanyDto> suggest(@RequestParam("keyword") String keyword) {
	    return service.getSuggest(keyword);
	}
	
	// 회사 수정 폼
	@GetMapping("/edit")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('ROOT')")
	public String editForm(int comId, Model model) {
		model.addAttribute("com", service.selectOneById(comId));
		return "/com/edit";
	}
	
	// 회사 수정 기능
	@PostMapping("/edit")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('ROOT')")
	public String edit(CompanyDto dto, 
			@RequestParam(value="logoFile", required=false) MultipartFile logoFile,
			Authentication auth,
			RedirectAttributes rttr) {
		String msg = "회사 정보 수정에 실패 하였습니다.";
		// 새 파일을 안 올렸을 때 기존 로고 URL을 유지하기 위해 수정 전 데이터를 먼저 조회
		try {
			CompanyDto before = service.selectOneById(dto.getComId());
			String oldLogoUrl = (before != null) ? before.getComLogo() : null;

			if (logoFile != null && !logoFile.isEmpty()) {
				FileUploadDto result = FileUploadUtil.upload(logoFile, FileUploadType.COMPANY_LOGO);
				dto.setComLogo(result.getFileUrl());
			} else {
				dto.setComLogo(oldLogoUrl);
			}
			if(service.update(dto) > 0) { 
				msg = "회사 정보 수정에 성공하셨습니다.";
				// 로고를 교체한 경우에만 기존 파일 정리
				if (logoFile != null && !logoFile.isEmpty()) {
					FileUploadUtil.delete(oldLogoUrl);
				}
			}
		} catch (FileUploadException e) {
			msg = e.getMessage();
		}
		
		rttr.addFlashAttribute("msg", msg);
		
		// 권한 문자열만 추출해서 비교
		CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
	    Set<String> authNames = user.getAuthorities().stream()
	            .map(GrantedAuthority::getAuthority)
	            .collect(Collectors.toSet());

	    boolean isRoot = authNames.contains("ROOT");
	    
	    if(isRoot) {
	    	return "redirect:/com/list";
	    } else {
	    	return "redirect:/com/my";
	    }
		
	}
	
	// 회사 삭제 폼
	@GetMapping("/delete")
	@PreAuthorize("hasAuthority('ROOT')")
	public String deleteModal(@RequestParam("comId") Integer comId, Model model) {
	    CompanyDto dto = service.selectOneById(comId);
	    model.addAttribute("com", dto);
		return "/com/delModal";
	}
	
	// 회사 삭제 기능
	@PostMapping("/delete")
	@PreAuthorize("hasAuthority('ROOT')")
	@ResponseBody
	public Map<String, Object> delete(Authentication auth, EmpDto dto) {
	    Map<String, Object> result = new HashMap<>();

	    //2. 관리자가 입력한 비밀번호가 일치 하지 않을 경우
	    CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
	    dto.setEmpId(user.getUser().getEmpId());
	    boolean matched = empService.matchPassword(dto);
	    if (!matched) {
	        result.put("success", false);
	        result.put("message", "비밀번호가 올바르지 않습니다.");
	        return result;
	    }

	    try {
	        service.delete(dto.getComId());
	        result.put("success", true);
	    } catch (IllegalArgumentException e) {
	        // 하위 부서 존재 등 비즈니스 로직 검증 실패
	        result.put("success", false);
	        result.put("message", e.getMessage());
	    }
	    
	    return result;
	}
	
	// 회사 정보 상세 조회
	@GetMapping("/detail")
	public String myDetail(@RequestParam("comId") int comId,
						   Model model) {
		//통계 데이터
		StatsDeptDto stats = deptService.selecStats(comId);
		List<DeptDto> deptList = deptService.selectOrgTree(comId);
		CompanyDto com = service.selectOneById(comId);
		
		model.addAttribute("stats", stats);
		model.addAttribute("com", com);
		model.addAttribute("deptList", deptList);
		return "/com/detail";
	}
	
	// 내 회사 정보 조회
	@GetMapping("/my")
	public String mycom(Principal prinipal, HttpSession session, Model model) {
		Integer empId = (Integer) session.getAttribute("empId");
		Integer comId = (Integer) session.getAttribute("comId");
		if(empId == null || comId == null) return "redirect:/auth/login";
		
		CompanyDto com = service.selectOneByEmpId(empId);
		//통계 데이터
		StatsDeptDto stats = deptService.selecStats(comId);
		List<DeptDto> deptList = deptService.selectOrgTree(comId);
				
		model.addAttribute("stats", stats);
		model.addAttribute("com", com);
		model.addAttribute("deptList", deptList);
		return "/com/mypage";
	}
}