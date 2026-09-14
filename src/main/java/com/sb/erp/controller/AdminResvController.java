package com.sb.erp.controller;

import java.util.List;
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

import com.sb.erp.dto.ResDto;
import com.sb.erp.dto.ResvDto;
import com.sb.erp.dto.ResvSearchDto;
import com.sb.erp.dto.StatsResvDto;
import com.sb.erp.security.CustomUserDetails;
import com.sb.erp.service.ReservationService;
import com.sb.erp.service.ResourceService;
import com.sb.erp.util.PagingUtil;

@Controller
@RequestMapping("/admin/resv")
@PreAuthorize("hasRole('ADMIN')")
public class AdminResvController {
	 @Autowired private ReservationService service;
	 @Autowired private ResourceService resService;

	// 관리자 예약 관리 목록
	    @GetMapping("/list")
	    public String list(ResvSearchDto search, 
	    				   Authentication auth, Model model) {
	    	// 현재 로그인 사용자 정보
	    	CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
	    	search.setComId(user.getUser().getComId());
	    	
	    	if (search.getStartDt() == null || search.getStartDt().isBlank()) {
	    		search.setStartDt(java.time.LocalDate.now().minusDays(30).toString());
	        }
	        if (search.getEndDt() == null || search.getEndDt().isBlank()) {
	        	search.setEndDt(java.time.LocalDate.now().toString());
	        }
	    	
	        int listtotal = service.getResvCount(search);
	        PagingUtil paging = new PagingUtil(listtotal, search.getPstartno());
	        List<ResvDto> list = service.getResvList(search);

	        // 통계용
	        StatsResvDto stats = service.countByStats(search);
	        model.addAttribute("totalCount", stats.getResvTotal());
	        model.addAttribute("waitCount", stats.getWaiTotal());
	        model.addAttribute("approveCount", stats.getAppTotal());
	        model.addAttribute("rejectCount", stats.getRejTotal());

	        
	        model.addAttribute("list", list);
	        model.addAttribute("paging", paging);
	        model.addAttribute("search", search);

	        return "resv/list";
	    }
	    
	    @GetMapping("/detail")
	    public String detail_get(Authentication auth, Integer revId, Model model) {
	    	// 현재 로그인 사용자 정보
	    	CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
		    Set<String> authNames = user.getAuthorities().stream()
		            .map(GrantedAuthority::getAuthority)
		            .collect(Collectors.toSet());

		    boolean isAdmin = authNames.contains("ROLE_ADMIN");
	    	
	        ResvDto resv = service.getResvDetail(revId);
	        ResDto dto = resService.getResourceDetail(resv.getResId());
	        
	        model.addAttribute("resource", dto);
	        model.addAttribute("resv", resv);
	        model.addAttribute("isAdmin", isAdmin);
	        model.addAttribute("empId", user.getUser().getEmpId());

	    	return "resv/detail";
	    }
	    
	    //
	    @PostMapping("/approve")
	    public String approve(ResvDto resvDto, Authentication auth) {
	    	// 현재 로그인 사용자 정보
	    	CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
	    	resvDto.setApprovedEmpId(user.getUser().getEmpId());
	    	
	    	int result = service.updateApprove(resvDto);
	        return "redirect:/admin/resv/list";
	    }

	    @PostMapping("/reject")
	    public String reject(ResvDto resvDto, Authentication auth) {
	    	// 현재 로그인 사용자 정보
	    	CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
	    	resvDto.setApprovedEmpId(user.getUser().getEmpId());

	        int result = service.updateReject(resvDto);
	        return "redirect:/admin/resv/list";
	    }
}
