package com.sb.erp.controller;

import java.util.HashMap;
import java.util.List;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sb.erp.dto.ResDto;
import com.sb.erp.dto.ResSearchDto;
import com.sb.erp.dto.ResvDto;
import com.sb.erp.dto.ResvSearchDto;
import com.sb.erp.security.CustomUserDetails;
import com.sb.erp.service.ReservationService;
import com.sb.erp.service.ResourceService;
import com.sb.erp.util.PagingUtil;

@Controller
@RequestMapping("/resv")
public class ReservationController {
    @Autowired private ReservationService service;
    @Autowired private ResourceService resService;

    // 내 예약 목록
    @GetMapping("/my")
    public String my_list(ResvSearchDto search, Authentication auth, Model model) {
    	// 현재 로그인 사용자 정보
    	CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
    	search.setComId(user.getUser().getComId());
    	search.setEmpId(user.getUser().getEmpId());
    	
    	if (search.getStartDt() == null || search.getStartDt().isBlank()) {
    		search.setStartDt(java.time.LocalDate.now().minusDays(30).toString());
    	}
    	if (search.getEndDt() == null || search.getEndDt().isBlank()) {
    		search.setEndDt(java.time.LocalDate.now().toString());
    	}
    	
    	int listtotal = service.getResvCount(search);
    	PagingUtil paging = new PagingUtil(listtotal, search.getPstartno());
    	List<ResvDto> list = service.getResvList(search);
    	
    	model.addAttribute("list", list);
    	model.addAttribute("paging", paging);
    	model.addAttribute("search", search);
    	
    	return "resv/my";
    }

    @GetMapping("/insert")
    public String insertForm(Authentication auth, Model model) {
    	// 현재 로그인 사용자 정보
    	CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
    	
    	ResSearchDto search = new ResSearchDto();
    	search.setComId(user.getUser().getComId());
    	
    	// 예약 할 수 있는 회사의 자원 정보
    	// TODO: 자원 정보에 대해 더 디테일하게 갯수 체크 해야될 것 같음
        List<ResDto> resList = resService.getResListForResv(search);
        model.addAttribute("resList", resList);

        return "resv/insert";
    }

    @PostMapping("/insert")
    public String insert(Authentication auth, ResvDto resvDto, RedirectAttributes rttr) {
    	// 현재 로그인 사용자 정보
    	CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
    	resvDto.setEmpId(user.getUser().getEmpId());
    	resvDto.setComId(user.getUser().getComId());
    	
    	try {
            service.insert(resvDto);
        } catch (IllegalStateException e) {
            // 예: "해당 기간에 예약 가능한 수량이 부족합니다. (남은 수량: 2개)"
        	rttr.addFlashAttribute("toastMsg", e.getMessage());
        	rttr.addFlashAttribute("toastKind", "warn");
        	rttr.addFlashAttribute("toastField", "quantity"); // 문제 필드 지목 (선택사항)
            return "redirect:/resv/insert";
        } catch (IllegalArgumentException e) {
        	rttr.addFlashAttribute("toastMsg", e.getMessage());
        	rttr.addFlashAttribute("toastKind", "warn");
            return "redirect:/resv/insert";
        }

    	rttr.addFlashAttribute("toastMsg", "예약이 신청되었습니다.");
    	rttr.addFlashAttribute("toastKind", "ok");
        return "redirect:/resv/my";
    }
    
    @GetMapping("/edit")
    public String edit_get(Authentication auth, Integer revId, Model model) {
        ResvDto resv = service.getResvDetail(revId);
        ResDto dto = resService.getResourceDetail(resv.getResId());
        model.addAttribute("resource", dto);
        model.addAttribute("resv", resv);
    	return "resv/edit";
    }
    
    @PostMapping("/edit")
    public String edit_post(ResvDto dto) {
    	int result = service.update(dto);
    	return "redirect:/resv/my";
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
    
    @PostMapping("/cancel")
    public String cancel_post(Integer revId) {
    	int result = service.delete(revId);
    	return "redirect:/resv/my";
    }
    
    //기간 선택 후 실시간 잔여수량 조회
    @GetMapping("/available")
    @ResponseBody
    public Map<String, Object> getAvailableQty(ResvSearchDto search, Authentication auth) {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        ResDto res = resService.getResourceDetail(search.getResId());

        // 소속 회사 재검증 (여기서도 반드시)
        if (res == null || !res.getComId().equals(user.getUser().getComId())) {
            throw new IllegalArgumentException("잘못된 자원 요청입니다.");
        }

        // 특정 자원의 특정 기간에 이미 예약(대기+승인)된 수량 합계
        int reservedQty = service.getReservedQuantity(search);
        int availableQty = res.getQuantity() - reservedQty;

        Map<String, Object> result = new HashMap<>();
        result.put("totalQuantity", res.getQuantity());
        result.put("reservedQty", reservedQty);
        result.put("availableQty", Math.max(availableQty, 0));
        result.put("resStatus", res.getResStatus());
        return result;
    }
    
}
