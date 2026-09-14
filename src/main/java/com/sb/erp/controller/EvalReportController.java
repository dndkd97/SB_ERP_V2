package com.sb.erp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sb.erp.dto.EvalPeriodDto;
import com.sb.erp.dto.EvalReportDto;
import com.sb.erp.dto.EvalReportSearchDto;
import com.sb.erp.service.DeptService;
import com.sb.erp.service.EvalPeriodService;
import com.sb.erp.service.EvalReportService;
import com.sb.erp.util.PagingUtil;
import com.sb.erp.util.SecurityUtil;

@Controller
public class EvalReportController {
	@Autowired EvalReportService evalReportService;
	@Autowired EvalPeriodService evalPeriodService;
	@Autowired DeptService deptService;

	// ─── 회차별 리포트 목록 (관리자용, 검색 + 페이징) ─────────────
	@GetMapping("/eval/report/list")
	public String list(EvalReportSearchDto search, Model model, RedirectAttributes ra) {

	    // periodId 미지정 → 회차 관리 화면으로 리다이렉트
	    if (search.getPeriodId() == null) {
	        ra.addFlashAttribute("infoMsg", 
	            "회차를 먼저 선택하세요. 마감된 회차의 상세 화면에서 'AI 리포트 보기'로 진입할 수 있습니다.");
	        return "redirect:/eval/period/list";
	    }

	    EvalPeriodDto period = evalPeriodService.selectByPeriodId(search.getPeriodId());
	    if (period == null) {
	        ra.addFlashAttribute("errorMsg", "존재하지 않는 회차입니다.");
	        return "redirect:/eval/period/list";
	    }

	    // CLOSED/REPORTED 상태만 리포트 조회 가능
	    String status = period.getPeriodStatus();
	    if (!"CLOSED".equals(status) && !"REPORTED".equals(status)) {
	        ra.addFlashAttribute("errorMsg", "마감(CLOSED) 이상 상태의 회차만 리포트를 조회할 수 있습니다.");
	        return "redirect:/eval/period/detail?periodId=" + search.getPeriodId();
	    }

	    // 페이징 계산 (12개씩)
	    int currentPage = (search.getPage() == null || search.getPage() < 1) ? 1 : search.getPage();
	    int total = evalReportService.countByPeriodSearch(search);
	    PagingUtil paging = new PagingUtil(total, currentPage, 12, 10);

	    // 페이징 값 세팅 후 조회
	    search.setPstartno(paging.getPstartno());
	    search.setOnepagelist(paging.getOnepagelist());
	    List<EvalReportDto> reports = evalReportService.searchByPeriod(search);

	    int totalEvals = evalPeriodService.countEvalsByPeriodId(search.getPeriodId());

	    model.addAttribute("period", period);
	    model.addAttribute("reports", reports);
	    model.addAttribute("reportCount", total); // 필터 후 총 건수
	    model.addAttribute("totalEvals", totalEvals);
	    model.addAttribute("paging", paging);
	    model.addAttribute("search", search);
	    model.addAttribute("deptList", deptService.selectAll());
	    return "eval/report/list";
	}


	// ─── 리포트 상세 (관리자/본인) ────────────────
	@GetMapping("/eval/report/detail")
	public String detail(@RequestParam int reportId,
	                     Model model, RedirectAttributes ra) {

		EvalReportDto report = evalReportService.selectByReportId(reportId);
		if (report == null) {
			ra.addFlashAttribute("errorMsg", "존재하지 않는 리포트입니다.");
			return "redirect:/eval/report/list";
		}

		// 관리자 또는 본인만 조회 가능
		int loginEmpId = SecurityUtil.getCurrentEmpId();
		if (report.getEmpId() != loginEmpId && !SecurityUtil.isAdmin()) {
			ra.addFlashAttribute("errorMsg", "본인 리포트만 조회할 수 있습니다.");
			return "redirect:/eval/report/my";
		}

		model.addAttribute("report", report);
		return "eval/report/detail";
	}


	// ─── 본인 리포트 이력 ─────────────────────────
	@GetMapping("/eval/report/my")
	public String my(Model model) {
		List<EvalReportDto> reports = evalReportService.selectMyAll();
		model.addAttribute("reports", reports);
		return "eval/report/my";
	}


	// ─── 관리자 수동 트리거: 회차 전체 리포트 재생성 ───
	@PostMapping("/eval/report/generate")
	public String generate(@RequestParam int periodId, RedirectAttributes ra) {
	    // 배치 흐름을 통해 실행 (REPORTED 상태에서도 진입 가능)
	    // - 웹 스레드 blocking 방지, 폴링 UI로 진행률 표시
	    // - 상태 전환: REPORTED → REPORTING → REPORTED (또는 REPORTING_FAILED)
	    int result = evalPeriodService.reportPeriod(periodId);
	    
	    if (result == -1) {
	        ra.addFlashAttribute("errorMsg", "존재하지 않는 회차입니다.");
	        return "redirect:/eval/report/list";
	    }
	    if (result == -2) {
	        ra.addFlashAttribute("errorMsg", "현재 상태에서는 리포트를 재생성할 수 없습니다. "
	                + "평가 진행 중이거나 이미 리포트 생성이 진행 중인 회차는 재생성 불가입니다.");
	        return "redirect:/eval/report/list?periodId=" + periodId;
	    }
	    
	    ra.addFlashAttribute("successMsg", "리포트 재생성을 시작합니다. 진행률은 회차 상세 페이지에서 확인하실 수 있습니다.");
	    // 폴링 UI가 있는 회차 상세로 리다이렉트
	    return "redirect:/eval/period/detail?periodId=" + periodId;
	}


	// ─── 관리자 수동 트리거: 특정 사원 리포트 개별 재생성 ───
	@PostMapping("/eval/report/regenerate")
	public String regenerate(@RequestParam int periodId,
	                         @RequestParam int empId,
	                         RedirectAttributes ra) {
		int result = evalReportService.regenerateReport(periodId, empId);
		return handleGenerateResult(result, periodId, ra);
	}


	// ─── 결과 처리 헬퍼 ─────────────────────────
	private String handleGenerateResult(int result, int periodId, RedirectAttributes ra) {
		if (result == 1) {
			ra.addFlashAttribute("successMsg", "AI 리포트를 생성했습니다.");
			return "redirect:/eval/report/list?periodId=" + periodId;
		}
		if (result == -1) {
			ra.addFlashAttribute("errorMsg", "존재하지 않는 회차입니다.");
			return "redirect:/eval/report/list";
		}
		if (result == -2) {
			ra.addFlashAttribute("errorMsg", "마감(CLOSED) 이상 상태의 회차에서만 리포트를 생성할 수 있습니다.");
			return "redirect:/eval/report/list?periodId=" + periodId;
		}
		if (result == -3) {
			ra.addFlashAttribute("errorMsg", "제출된 평가가 없어 리포트를 생성할 수 없습니다.");
			return "redirect:/eval/report/list?periodId=" + periodId;
		}
		ra.addFlashAttribute("errorMsg", "리포트 생성 중 오류가 발생했습니다.");
		return "redirect:/eval/report/list?periodId=" + periodId;
	}
}
