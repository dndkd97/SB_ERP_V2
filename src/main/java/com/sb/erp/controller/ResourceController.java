package com.sb.erp.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sb.erp.dto.CompanyDto;
import com.sb.erp.dto.EmpDto;
import com.sb.erp.dto.ResSearchDto;
import com.sb.erp.security.CustomUserDetails;
import com.sb.erp.dto.ResDto;
import com.sb.erp.service.EmpService;
import com.sb.erp.service.ReservationService;
import com.sb.erp.service.ResourceService;
import com.sb.erp.util.PagingUtil;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/res")
public class ResourceController {
    @Autowired private ResourceService service;
    @Autowired private ReservationService resvService;
    @Autowired private EmpService empService;

    // 자원 관리 목록 페이지
    @GetMapping("/list")
    public String list(ResSearchDto search, Authentication auth, Model model) {
    	CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
    	search.setComId(user.getUser().getComId());
    	
		int listtotal = service.getResourceCount(search);
		// 검색 조건이 null
		boolean isEmpty = !search.hasSearchCondition();
		
		List<ResDto> list = new ArrayList<>();
		PagingUtil paging;
		
		if(isEmpty) {
	    	paging = new PagingUtil(0, search.getPstartno());
		}else {
			paging = new PagingUtil(listtotal, search.getPstartno(), search.getOnepagelist(), 10);
			list = service.getResourceList(search);
		}
    	
        model.addAttribute("resourceList", list);
        model.addAttribute("paging", paging);
        model.addAttribute("search", search);

        return "res/list";
    }

    // 자원 관리 상세 페이지
    @GetMapping("/detail")
    @PreAuthorize("hasRole('ADMIN')")
    public String detail(@RequestParam("resId") int resId,
                         @RequestParam(value = "error", required = false) String error,
                         Model model) {
        ResDto dto = service.getResourceDetail(resId);
        model.addAttribute("res", dto);
        model.addAttribute("error", error);
        return "res/detail";
    }

    // 자원 관리 등록 페이지
    @GetMapping("/insert")
    @PreAuthorize("hasRole('ADMIN')")
    public String insertForm() {
        return "res/insert";
    }

    // 자원 관리 등록 기능
    @PostMapping("/insert")
    @PreAuthorize("hasRole('ADMIN')")
    public String insert(ResDto resDto, Authentication auth, RedirectAttributes rttr) {
    	String msg = "자원관리 등록실패";
    	// 현재 로그인 사용자 정보
    	CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
    	resDto.setComId(user.getUser().getComId());
    	
    	if(service.insertResource(resDto) > 0) { msg = "자원 관리 등록 성공"; }
    	
    	rttr.addFlashAttribute("msg",msg);
        return "redirect:/res/list";
    }

    // 자원 관리 수정 페이지
    @GetMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateForm(@RequestParam("resId") int resId, Model model) {
        ResDto resourceDto = service.getResourceDetail(resId);
        model.addAttribute("resource", resourceDto);
        return "res/update";
    }

    // 자원 관리 수정 등록
    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(ResDto resourceDto) {
    	service.updateResource(resourceDto);
        return "redirect:/res/list";
    }

    // 자원 관리 삭제 기능
    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam("resId") Integer resId, Authentication auth, EmpDto dto) {
    	Map<String, Object> result = new HashMap<>();
    	
    	// 현재 로그인 사용자 정보
    	CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
    	dto.setEmpId(user.getUser().getEmpId());

    	// 입력한 비밀번호와 저장된 비밀 번호가 불일치 (평문, 암호화)
    	boolean matched = empService.matchPassword(dto);
 	    if (!matched) {
 	        result.put("success", false);
 	        result.put("message", "비밀번호가 올바르지 않습니다.");
 	        return result;
 	    }
        // 예약 처리 중인 자원의 경우 삭제 불가
 	    int resvRes = resvService.countReservationsByResourceId(resId); 
        if (resvRes> 0) {
        	result.put("success", false);
        	result.put("reason", "hasReservations");
 	        result.put("message", "이 자원에는 진행 중인 예약이 "+resvRes+"건 있습니다. 예약을 먼저 취소하거나 완료한 뒤 다시 시도해주세요.");
 	        return result;
        }

        service.deleteResource(resId);
        result.put("success", true);
	    return result;
    }
    
    //자원코드 중복 체크
    @GetMapping("/checkCode")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> checkCode(String resCode, Authentication auth){
    	CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
    	
    	ResDto dto = new ResDto();
    	dto.setComId(user.getUser().getComId());
    	dto.setResCode(resCode);
    	
		Map<String, Object> res = new HashMap<>();
		ResDto rdto = service.isDuplicateResCode(dto);
		
		if(rdto != null) res.put("duplicate", true);
		else res.put("duplicate", false);
		
		return res;
	}
}
