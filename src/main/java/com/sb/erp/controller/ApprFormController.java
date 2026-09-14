package com.sb.erp.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sb.erp.api.OpenAiGpt;
import com.sb.erp.dto.ApprFormDto;
import com.sb.erp.dto.ApprFormSearchDto;
import com.sb.erp.dto.CompanyDto;
import com.sb.erp.dto.EmpDto;
import com.sb.erp.security.CustomUserDetails;
import com.sb.erp.service.ApprFormService;
import com.sb.erp.service.CompanyService;
import com.sb.erp.service.EmpService;
import com.sb.erp.util.PagingUtil;

@Controller
@RequestMapping("/appr")
public class ApprFormController { 
	@Autowired ApprFormService appr;
	@Autowired CompanyService com;
	@Autowired OpenAiGpt gpt;
	@Autowired EmpService empService;
	
	// 회사 검색 기능
	@GetMapping("/searchCompany")
	@ResponseBody
	public List<CompanyDto> searchCompany(@RequestParam("company") String company){
		//return appr.searchCompany(company);
		return com.getSuggest(company);
	}
	
	// 양식 코드 중복 검사 기능
	@GetMapping("/checkCode")
	@ResponseBody
	public Map<String,Object> checkCode(@RequestParam("code") String code,
										@RequestParam("comId") String comId,
								@RequestParam(value ="forId", required = false) Integer forId) {
		
		Map<String,Object> result = new HashMap<>();
		
		// null들어오면 뻑나는거 방지
		if(comId == null || comId.trim().isEmpty()) {
			result.put("checkCode", false);
			return result;
		}
		
		ApprFormDto dto = new ApprFormDto();
		dto.setComId(Integer.parseInt(comId));
		dto.setForCode(code);
		
		if(forId != null) { 
			dto.setForId(forId);
		}
		
		String find = appr.findByCode(dto);
		
		if(find != null) {
			result.put("checkCode", false);
		}
		else {
			result.put("checkCode", true);
		}
		
		return result;
	}
	
	// 양식 리스트 입력한 값 찾기
	// required = false : 필수 값이 아니라고 설정
    // defaultValue = "" : 값이 안 넘어오면 디폴트값 지정
	@GetMapping("/list_form")
	public String searchForms(ApprFormSearchDto search, Model model,
							  @RequestParam( value = "page", defaultValue = "1") int page){
		
		int totalCnt = 0;
	    List<ApprFormDto> list = new ArrayList<>();
	    PagingUtil paging;
	    
	    //2) 검색 조건이 null이 아닐때 실행
	    if(search.hasSearchCondition() || page > 1) {
	    	
	    	// 전체 양식 수
	    	totalCnt = appr.listFormCnt(search);
	    	paging = new PagingUtil(totalCnt, page);
	    	int offset = (page - 1) * paging.getOnepagelist();
	    	search.setPstartno(offset);
	    	
	    	list = appr.selectFormList(search);
	    } else {
	    	paging = new PagingUtil(0, page);
	    }
		
		// jsp로 데이터 보내기
		model.addAttribute("list", list);
		model.addAttribute("paging", paging);
		model.addAttribute("search", search);
		
		return "appr/list_form";
	} 

	
	// 양식 작성 폼
	@GetMapping("/write_form")
	public String writeForm() {
		return "appr/write_form";
	}
	
	// 양식 작성 처리
	@PostMapping("/write_form")
	public String writeForm_post(ApprFormDto dto, RedirectAttributes rttr, Model model) {
		try {
			// 양식 작성 성공
			if(appr.insertForm(dto) > 0) {
				rttr.addFlashAttribute("toastType", "success");
				rttr.addFlashAttribute("toastMsg", "결재 양식이 등록되었습니다.");
				return "redirect:/appr/list_form";
			}
		} catch (IllegalArgumentException e) {
			model.addAttribute("toastType", "error");
			model.addAttribute("toastMsg", e.getMessage());
			model.addAttribute("dto", dto);
		}
		return "appr/write_form";
	}
	
	// 양식 상세보기
	@GetMapping("/detail_form")
	public String detail(Model model,
						 @RequestParam("forId") int forId,
						 @RequestParam("forVersion") int forVersion) {
		
		// 받아온 값 dto 에 저장하여 select문으로 데이터 가져옴
		ApprFormDto param = new ApprFormDto();
		param.setForId(forId);
		param.setForVersion(forVersion);
		
		ApprFormDto dto = appr.selectFormAll(param);

		dto.setComName(appr.getCompanyName(dto.getComId()));
		
		model.addAttribute("dto", dto);
		return "appr/detail_form";
	}
	
	// 양식 수정 폼
	@GetMapping("/update_form")
	public String update(Model model,
						 @RequestParam("forId") int forId,
						 @RequestParam("forVersion") int forVersion) {
		
		ApprFormDto param = new ApprFormDto();
		param.setForId(forId);
		param.setForVersion(forVersion);
		
		// 클릭한 해당 양식의 id값으로 데이터 담아서 jsp value들 채울 용도
		ApprFormDto dto = appr.selectFormAll(param);
		dto.setComName(appr.getCompanyName(dto.getComId()));
				
		// update_form.jsp 로 데이터 보내기
		model.addAttribute("dto", dto);
		return "appr/update_form";
	}
	
	// 양식 수정 처리
	@PostMapping("/update_form")
	public String update_post(ApprFormDto dto, Model model, RedirectAttributes rttr) {
		
		try {
			// 양식 수정 성공
			if(appr.updateForm(dto) > 0) {
				rttr.addFlashAttribute("toastType", "success");
				rttr.addFlashAttribute("toastMsg", "결재 양식이 수정되었습니다.");
				return "redirect:/appr/list_form";
			}
		} catch (IllegalArgumentException e) {
			model.addAttribute("toastType", "error");
			model.addAttribute("errorMsg", e.getMessage());
		}
		
		ApprFormDto fail = new ApprFormDto();
		fail.setForId(dto.getForId());
		fail.setForVersion(dto.getForVersion());
		
		ApprFormDto result = appr.selectFormAll(fail);
		
		if(result != null) {
			result.setComName(appr.getCompanyName(result.getComId()));
		}
		
		model.addAttribute("dto", result);
		return "appr/update_form";
	}
	
	// 양식 삭제 처리
	@PostMapping("/deleteFormConfirm")
	@ResponseBody
	public Map<String, Object> delete(@RequestParam("forId") int forId,
									  @RequestParam("forVersion") int forVersion,
								 	  @RequestParam("empPass") String empPass,
								  	  @AuthenticationPrincipal CustomUserDetails userDetails) {
		
		Map<String, Object> result = new HashMap<>();
		
		EmpDto emp = new EmpDto();
		emp.setEmpId(userDetails.getUser().getEmpId());
		emp.setEmpPass(empPass);
		
		boolean matched = empService.matchPassword(emp);
		if(!matched) {
			result.put("success", false);
			result.put("message", "비밀번호가 올바르지 않습니다.");
			return result;
		}
		
		ApprFormDto dto = new ApprFormDto();
		dto.setForId(forId);
		dto.setForVersion(forVersion);
		appr.deleteForm(dto);

		result.put("success", true);
		return result;
	}
	
	// openAi 호출
	@PostMapping("/formSchema")
	@ResponseBody
	public Map<String, Object> formSchema(@RequestBody Map<String, String> body) {
		String userPrompt = body.get("prompt");
		Map<String, Object> result = new HashMap<>();
		
		try {
			String schemaJson = gpt.formSchema(userPrompt);
			result.put("success", true);
			result.put("schema", schemaJson);
		} catch (Exception e) {
			result.put("success", false);
			result.put("message", "AI 양식 생성에 실패했습니다");
		}
		return result;
	}
	
	// 특정 양식 전체 버전 이력 조회 (버전별 삭제)
	@GetMapping("/getFormVersions")
	@ResponseBody
	public List<ApprFormDto> getFormVersions(@RequestParam("forId") int forId) {
		return appr.selectFormVersions(forId);
	}
	
	
	
}
