package com.sb.erp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sb.erp.dto.DeptDto;
import com.sb.erp.dto.DeptTransferExecuteForm;
import com.sb.erp.dto.DeptTransferImpactDto;
import com.sb.erp.dto.DeptTransferLogDto;
import com.sb.erp.dto.DeptTransferLogSearchDto;
import com.sb.erp.dto.PendingDeptDto;
import com.sb.erp.security.CustomUserDetails;
import com.sb.erp.service.DeptService;
import com.sb.erp.service.DeptTransferService;
import com.sb.erp.util.PagingUtil;

@Controller
@RequestMapping("/dept/transfer")
@PreAuthorize("hasRole('ADMIN')")
public class DeptTransferController {
	@Autowired DeptTransferService service;
	@Autowired DeptService deptService;
	
    /** Authentication의 principal에서 로그인한 관리자의 회사(com_id)를 꺼낸다. */
    private Integer resolveComId(Authentication authentication) {
    	CustomUserDetails auth = (CustomUserDetails)authentication.getPrincipal();
    	return auth.getUser().getComId();
    }
	
    /** Authentication의 principal에서 로그인한 관리자의 emp_id를 꺼낸다 (이관 처리자 감사기록용). */
    private Integer resolveEmpId(Authentication authentication) {
    	CustomUserDetails auth = (CustomUserDetails)authentication.getPrincipal();
    	return auth.getUser().getEmpId();
    }
	
	
	@GetMapping("list")
	public String list(@RequestParam Integer deptId, Authentication authentication, Model model) throws IllegalAccessException {
		// 영향도 조회
	    int comId = resolveComId(authentication); 
	    DeptTransferImpactDto impact = service.getImpact(comId, deptId);
	    
	    model.addAttribute("impact", impact);
	    model.addAttribute("comId", comId);
		return "dept/transfer/list";
	}
	
    /** 이관 취소 — 부서를 다시 ACTIVE 상태로 되돌림 */
    @PostMapping("/cancel")
    public String cancel(@RequestParam Integer deptId, Authentication authentication, RedirectAttributes rttr) throws IllegalAccessException {
    	String msg = "부서 이관을 취소 처리하는데 실패했습니다.";
    	
    	int result = service.cancelTransfer(resolveComId(authentication), deptId);
    	if(result > 0) { msg = "부서 삭제를 취소 했습니다."; }
    	rttr.addFlashAttribute("msg", msg);
        return "redirect:/dept/list";
    }
    
    /** 이관 최종 실행 (단일 트랜잭션, 실패 시 전체 롤백) */
    @PostMapping("/execute")
    public String execute(@ModelAttribute DeptTransferExecuteForm form,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) throws IllegalAccessException {
        // 화면에서 넘어온 comId는 신뢰하지 않고, 인증 정보의 comId로 강제 치환한다
        // (다른 회사 comId를 조작해서 보내는 요청을 막기 위함)
    	Integer comId = resolveComId(authentication);
    	Integer empId = resolveEmpId(authentication);
        form.setComId(comId);
 
        try {
        	service.executeTransfer(form, empId);
            redirectAttributes.addFlashAttribute("message", "사원 이관이 완료되었습니다.");
        } catch (Exception e) {
            // 실패 원인을 사용자에게 그대로 전달 (전체 롤백은 서비스의 @Transactional 이 보장)
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dept/transfer/list?deptId=" + form.getDeptId();
        }
        return "redirect:/dept/list";
    }
    
    /**
     * 이관 대기(PENDING_DELETE) 부서 목록 페이지.
     * 이관 도중 다른 화면으로 이동했다가도, 여기로 다시 들어와 어떤 부서가 이관 대기중인지
     * 찾아서 재진입할 수 있도록 하는 진입점.
     */
    @GetMapping("/pending")
    public String pendingList(@RequestParam(required = false) String keyword,
                               Authentication authentication,
                               Model model) {
        Integer comId = resolveComId(authentication);
        List<PendingDeptDto> pendingDepts = service.getPendingTransferDepts(comId, keyword);
 
        model.addAttribute("pendingDepts", pendingDepts);
        model.addAttribute("keyword", keyword);
        return "dept/transfer/pending";
    }
    
    /**
     * 부서 이관 이력 화면 — 원부서/대상부서/처리일자/AI제안여부 필터.
     * @ModelAttribute 로 GET 쿼리스트링(originDeptId, targetDeptId, aiRecommended, dateFrom, dateTo)을 바로 바인딩.
     */
    @GetMapping("/log")
    public String transferLog(@ModelAttribute DeptTransferLogSearchDto search,
                               Authentication authentication,
                               Model model) {
        Integer comId = resolveComId(authentication);
        
        if (search.getDateFrom() == null || search.getDateFrom().isBlank()) {
        	search.setDateFrom(java.time.LocalDate.now().minusDays(30).toString());
        }
        if (search.getDateTo() == null || search.getDateTo().isBlank()) {
        	search.setDateTo(java.time.LocalDate.now().toString());
        }
        
        int listtotal = service.listTotal(comId, search);
        PagingUtil paging = new PagingUtil(listtotal, search.getPstartno(), search.getOnepagelist(), 10);
        
        // 부서 이관 이력 조회
        List<DeptTransferLogDto> logs = service.searchTransferLogs(comId, search);
        // 해당 회사의 부서 목록 조회
        List<DeptDto> deptOptions = deptService.getAllDeptsByComId(comId);
 
        model.addAttribute("paging", paging);
        model.addAttribute("logs", logs);
        model.addAttribute("deptOptions", deptOptions);
        model.addAttribute("search", search);
        return "dept/transfer/log";
    }
    
    
}
