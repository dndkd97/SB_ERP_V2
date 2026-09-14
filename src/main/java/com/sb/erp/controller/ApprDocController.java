package com.sb.erp.controller;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sb.erp.dto.ApprDocDto;
import com.sb.erp.dto.ApprDocInitResponseDto;
import com.sb.erp.dto.ApprFormDto;
import com.sb.erp.dto.ApprLineDto;
import com.sb.erp.dto.ApprLineRequestDto;
import com.sb.erp.dto.DeptDto;
import com.sb.erp.security.CustomUserDetails;
import com.sb.erp.service.ApprDocService;
import com.sb.erp.service.ApprLineRequestService;
import com.sb.erp.service.DeptService;
import com.sb.erp.util.PagingUtil;

@Controller
@RequestMapping("/appr")
public class ApprDocController {
	
	@Autowired ApprDocService service;
	@Autowired DeptService deptService;
	@Autowired ApprLineRequestService lineReqService;
	
	
	//////////////////////////// 문서 작성 처리 파트 /////////////////////////////
	
	// 해당 회사에 있는 활성화된 양식 가져오기
	@GetMapping("/getFormList")
	@ResponseBody
	public List<ApprFormDto> getFormList(@RequestParam("comId") int comId){
		ApprDocDto dto = new ApprDocDto();
		dto.setComId(comId);
		return service.findForm(dto);
	}
	
	// 문서 작성시 
	@GetMapping("/getFormDetail")
	@ResponseBody
	public ApprFormDto getFormDetail(@RequestParam("forId") int forId,
							   		 @RequestParam("forVersion") int forVersion) {
		Map<String, Object> params = new HashMap<>();
		params.put("forId", forId);
		params.put("forVersion", forVersion);

		return service.getForm(params);
	}
	
	// 문서 작성 폼
	@GetMapping("/write_doc")
	public String writeDoc(Model model,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		// 데이터 넣고 페이지 들어갈때 값 가져와서 삽입
		ApprDocDto dto = new ApprDocDto();
		dto.setEmpId(userDetails.getUser().getEmpId());
		
		ApprDocInitResponseDto result = service.initResponse(dto);
		model.addAttribute("dto", result);
		return "appr/write_doc";
	}
	
	// 문서 작성 처리
	@PostMapping("/write_doc")
	public String writeDoc_post(ApprDocDto dto,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			RedirectAttributes rttr,
			Model model) {
		
		dto.setEmpId(userDetails.getUser().getEmpId());
		dto.setComId(userDetails.getUser().getComId());
		
		boolean result = service.insertLines(dto);
		
		// 위 호출에서 성공시
		if(result) {
			rttr.addFlashAttribute("toastType", "success");
			rttr.addFlashAttribute("toastMsg", "결재 문서가 기안되었습니다.");
			return "redirect:/appr/list_doc";
		}
		
		ApprDocInitResponseDto reload = service.initResponse(dto);
		model.addAttribute("dto", reload);
		model.addAttribute("toastType", "error");
		model.addAttribute("toastMsg", "결재 문서 기안에 실패했습니다.");
		return "appr/write_doc";
	}
	
	//////////////////////////// 문서 작성 처리 파트 /////////////////////////////
	
	//////////////////////////// 문서 조회 처리 파트 /////////////////////////////
	
	@GetMapping("/list_doc")
	public String listDoc(Model model,
			@RequestParam(defaultValue = "history") String tab,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "1") int page,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		// 로그인 유저 정보 가져오기 (emp_id)
		ApprDocDto dto = new ApprDocDto(); 
		dto.setEmpId(userDetails.getUser().getEmpId());
		dto.setSearchKeyword(keyword);
		dto.setSearchStatus(status);
		
		int myTodoCnt = service.selectMyTodoDocsCnt(dto);
		
		// 카운트
		Map<String, Object> docCnts = service.selectDocCnt(dto);

		int totalCnt = "todo".equals(tab) ?
				myTodoCnt :
				service.selectMyHistoryDocsCnt(dto);
		

		PagingUtil paging = new PagingUtil(totalCnt, page);
		dto.setPstartno(paging.getPstartno());
		dto.setOnepagelist(paging.getOnepagelist());
		
		List<Map<String, Object>> hisDocs = List.of();
		List<Map<String, Object>> todoDocs = List.of();
		
		if("todo".equals(tab)) {
			todoDocs = service.selectMyTodoDocs(dto);
		}
		else {
			hisDocs = service.selectMyHistoryDocs(dto);
		}
		
		if("request".equals(tab)) {
			model.addAttribute("pendingRequests", lineReqService.getPendingRequests());
		}
		
		model.addAttribute("pendingCnt", lineReqService.countPending());
		model.addAttribute("paging", paging);
		model.addAttribute("docCnts", docCnts);
		model.addAttribute("myTodoCnt", myTodoCnt);
		model.addAttribute("hisDocs", hisDocs);
		model.addAttribute("todoDocs", todoDocs);
		model.addAttribute("activeTab", tab);
		model.addAttribute("status", status);
		model.addAttribute("keyword", keyword);
		model.addAttribute("page", page);
		
		return "appr/list_doc";
	}
	
	//////////////////////////// 문서 조회 처리 파트 /////////////////////////////

	//////////////////////////// 문서 승인,반려 처리 ///////////////////////////////
	
	// 상세보기 폼
	@GetMapping("/detail_doc")
	public String detailDoc(@AuthenticationPrincipal CustomUserDetails userDetails,
						    @RequestParam("docId") int docId,
							Model model) {
		
		// docId 사용하여 문서 정보 가져옴
		ApprDocDto doc = service.selectDocDetail(docId);
		// 결재선 가져오기
		List<ApprLineDto> lines = service.selectLinesByDocId(docId);
		
		// 로그인 한사람 empId 가져와야함
		int empId = userDetails.getUser().getEmpId();
		// 전체 결재선 목록에 결재상태가 'WAI' 인 데이터가 있나 검증
		boolean canProcess = lines.stream()
				.anyMatch(l -> l.getEmpId() == empId && "WAI".equals(l.getLinStatus()));
		
		model.addAttribute("doc", doc);
		model.addAttribute("lines", lines);
		model.addAttribute("canProcess", canProcess);
		
		return "appr/detail_doc";
	}
	
	// 승인 처리
	@PostMapping("/detail_doc/app")
	public String detailDoc_app(@RequestParam("docId") int docId,
				@AuthenticationPrincipal CustomUserDetails userDetails,
				RedirectAttributes rttr) {
		try {
			service.processLine(docId, userDetails.getUser().getEmpId(), "APP");
			rttr.addFlashAttribute("toastType", "success");
			rttr.addFlashAttribute("toastMsg", "결재를 승인했습니다.");
		} catch (ConcurrentModificationException e) {
			rttr.addFlashAttribute("toastType", "error");
			rttr.addFlashAttribute("toastMsg", e.getMessage());
		}
		return "redirect:/appr/detail_doc?docId=" + docId;
	}
	
	// 반려 처리
	@PostMapping("/detail_doc/rej")
	public String detailDoc_rej(@RequestParam("docId") int docId,
				@AuthenticationPrincipal CustomUserDetails userDetails,
				RedirectAttributes rttr) {
		try {
			service.processLine(docId, userDetails.getUser().getEmpId(), "REJ");
			rttr.addFlashAttribute("toastType", "success");
			rttr.addFlashAttribute("toastMsg", "결재를 반려했습니다.");
		} catch (ConcurrentModificationException e) {
			rttr.addFlashAttribute("toastType", "error");
			rttr.addFlashAttribute("toastMsg", e.getMessage());
		}
		return "redirect:/appr/detail_doc?docId=" + docId;
	}
	
	//////////////////////////// 문서 승인,반려 처리 ///////////////////////////////
	
	//////////////////////////// 결재선 파트 ///////////////////////////////
	
	@GetMapping("/getApprLines")
	@ResponseBody
	public List<ApprLineDto> getApprLines(@RequestParam("isImportant") boolean isImportant,
				@AuthenticationPrincipal CustomUserDetails userDetails)	{
		ApprDocDto dto = new ApprDocDto();
		dto.setEmpId(userDetails.getUser().getEmpId());
		
		return service.approversByEmpId(dto);
	}
	
	// 결재선 지정용 부서 트리
	@GetMapping("/getDeptTree")
	@ResponseBody
	public List<DeptDto> getDeptTree(@AuthenticationPrincipal CustomUserDetails userDetails){
		return service.cntApprovers(
				userDetails.getUser().getDeptId(),
				userDetails.getUser().getEmpId()
		);
	}
	
	// 특정 부서 소속 사원 목록
	@GetMapping("/getDeptEmps")
	@ResponseBody
	public List<ApprLineDto> getDeptEmps(@RequestParam("deptId") int deptId) {
		return service.selectDeptEmpsForLines(deptId);
	}
	
	// 결재선 변경 요청 등록
	@PostMapping("/requsetLineChange")
	@ResponseBody
	public Map<String, Object> requestLineChange(
			@RequestParam("docId") int docId,
			@RequestParam("linId") int linId,
			@RequestParam("oriEmpId") int oriEmpId,
			@RequestParam("reqReason") String reqReason,
			@AuthenticationPrincipal CustomUserDetails userDetails){
		
		Map<String, Object> result = new HashMap<>();
		
		ApprLineRequestDto dto = new ApprLineRequestDto();
		dto.setDocId(docId);
		dto.setLinId(linId);
		dto.setOriEmpId(oriEmpId);
		dto.setReqEmpId(userDetails.getUser().getEmpId());
		dto.setReqReason(reqReason);
		
		lineReqService.requestChange(dto);
		result.put("succescc", true);
		return result;
	}
	
	// 관리자 승인
	@PostMapping("/approveLineChange")
	@ResponseBody
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('ROOT')")
	public Map<String, Object> approveLineChange(
			@RequestParam("reqId") int reqId,
			@RequestParam("newEmpId") int newEmpId,
			@AuthenticationPrincipal CustomUserDetails userDetails){
	
		Map<String, Object> result = new HashMap<>();
		boolean success = lineReqService.approveRequest(reqId, newEmpId, newEmpId);
		result.put("success", success);
		if(!success) result.put("message", "이미 처리되었거나 존재하지 않는 요청입니다.");
		return result;
	}
	
	// 관리자 반려
	@PostMapping("/rejectLineChange")
	@ResponseBody
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('ROOT')")
	public Map<String, Object> rejectLineChange(
			@RequestParam("reqId") int reqId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		Map<String, Object> result = new HashMap<>();
		boolean success = lineReqService.rejectRequest(reqId, reqId);
		result.put("success", success);
		return result;
	}
	
	
	//////////////////////////// 결재선 파트 ///////////////////////////////
	
}
