package com.sb.erp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sb.erp.dto.EvalDto;
import com.sb.erp.dto.EvalPeriodDto;
import com.sb.erp.dto.EvalPeriodSearchDto;
import com.sb.erp.service.EvalPeriodService;
import com.sb.erp.service.EvalService;
import com.sb.erp.util.SecurityUtil;

@Controller
public class EvalController {

    @Autowired EvalService evalService;
    @Autowired EvalPeriodService evalPeriodService;

 // ─── 평가 대시보드 (부서장이 평가할 대상 목록) ───────────
    @GetMapping("/eval/list")
    public String list(@RequestParam(required = false) Integer periodId,
                       Model model, RedirectAttributes ra) {
        
        // 회차 없으면 회차 선택 화면 (OPEN 회차 카드 목록)
        if (periodId == null) {
            EvalPeriodSearchDto search = new EvalPeriodSearchDto();
            search.setPeriodStatus("OPEN");
            List<EvalPeriodDto> openPeriods = evalPeriodService.search(search);
            
            model.addAttribute("period", null);
            model.addAttribute("openPeriods", openPeriods);
            return "eval/list";
        }
        
        EvalPeriodDto period = evalPeriodService.selectByPeriodId(periodId);
        if (period == null) {
            ra.addFlashAttribute("errorMsg", "존재하지 않는 회차입니다.");
            return "redirect:/eval/list";
        }
        
        List<EvalDto> targets = evalService.selectTargetsByCurrentEvaluator(periodId);
        int submittedCount = evalService.countMySubmitted(periodId);
        
        model.addAttribute("period", period);
        model.addAttribute("targets", targets);
        model.addAttribute("submittedCount", submittedCount);
        model.addAttribute("totalCount", targets.size());
        return "eval/list";
    }

    // ─── 평가 작성 폼 ─────────────────────────────
    @GetMapping("/eval/add")
    public String addForm(@RequestParam int periodId,
                          @RequestParam int targetEmpId,
                          Model model, RedirectAttributes ra) {

        EvalPeriodDto period = evalPeriodService.selectByPeriodId(periodId);
        if (period == null) {
            ra.addFlashAttribute("errorMsg", "존재하지 않는 회차입니다.");
            return "redirect:/eval/list";
        }

        if (!"OPEN".equals(period.getPeriodStatus())) {
            ra.addFlashAttribute("errorMsg", "진행 중인 회차만 평가를 작성할 수 있습니다.");
            return "redirect:/eval/list?periodId=" + periodId;
        }

        model.addAttribute("period", period);
        model.addAttribute("targetEmpId", targetEmpId);
        return "eval/add";
    }

    // ─── 평가 임시저장 ─────────────────────────────
    @PostMapping("/eval/saveDraft")
    public String saveDraft(EvalDto dto, RedirectAttributes ra) {
        int result = evalService.saveDraft(dto);
        return handleResult(result, dto.getPeriodId(), "임시 저장했습니다.", ra);
    }

    // ─── 평가 제출 ─────────────────────────────
    @PostMapping("/eval/submit")
    public String submit(EvalDto dto, RedirectAttributes ra) {
        int result = evalService.submit(dto);
        return handleResult(result, dto.getPeriodId(), "평가를 제출했습니다.", ra);
    }

    // ─── 평가 수정 폼 (임시 저장분 이어서 작성) ─────────────
    @GetMapping("/eval/edit")
    public String editForm(@RequestParam int evalId,
                           Model model, RedirectAttributes ra) {

        EvalDto eval = evalService.selectByEvalId(evalId);
        if (eval == null) {
            ra.addFlashAttribute("errorMsg", "존재하지 않는 평가입니다.");
            return "redirect:/eval/list";
        }

        // 본인이 작성한 평가만 수정 가능
        if (eval.getEvaluatorId() != SecurityUtil.getCurrentEmpId()) {
            ra.addFlashAttribute("errorMsg", "본인이 작성한 평가만 수정할 수 있습니다.");
            return "redirect:/eval/list?periodId=" + eval.getPeriodId();
        }

        EvalPeriodDto period = evalPeriodService.selectByPeriodId(eval.getPeriodId());
        if (period == null || !"OPEN".equals(period.getPeriodStatus())) {
            ra.addFlashAttribute("errorMsg", "진행 중인 회차만 수정할 수 있습니다.");
            return "redirect:/eval/list";
        }

        model.addAttribute("period", period);
        model.addAttribute("eval", eval);
        return "eval/edit";
    }

    // ─── 상세 조회 (제출한 평가 확인) ─────────────
    @GetMapping("/eval/detail")
    public String detail(@RequestParam int evalId,
                         Model model, RedirectAttributes ra) {

        EvalDto eval = evalService.selectByEvalId(evalId);
        if (eval == null) {
            ra.addFlashAttribute("errorMsg", "존재하지 않는 평가입니다.");
            return "redirect:/eval/list";
        }

        // 평가자 본인 또는 관리자만 조회 가능
        int loginEmpId = SecurityUtil.getCurrentEmpId();
        if (eval.getEvaluatorId() != loginEmpId && !SecurityUtil.isAdmin()) {
            ra.addFlashAttribute("errorMsg", "본인이 작성한 평가만 조회할 수 있습니다.");
            return "redirect:/eval/list?periodId=" + eval.getPeriodId();
        }

        model.addAttribute("eval", eval);
        return "eval/detail";
    }

    // ─── 결과 처리 헬퍼 ────────────────────────────
    private String handleResult(int result, int periodId, String successMsg, RedirectAttributes ra) {
        if (result == 1) {
            ra.addFlashAttribute("successMsg", successMsg);
            return "redirect:/eval/list?periodId=" + periodId;
        }
        if (result == -1) {
            ra.addFlashAttribute("errorMsg", "존재하지 않는 회차입니다.");
            return "redirect:/eval/list";
        }
        if (result == -2) {
            ra.addFlashAttribute("errorMsg", "진행 중인 회차만 평가할 수 있습니다.");
            return "redirect:/eval/list?periodId=" + periodId;
        }
        if (result == -3) {
            ra.addFlashAttribute("errorMsg", "평가 대상이 잘못되었습니다.");
            return "redirect:/eval/list?periodId=" + periodId;
        }
        if (result == -4) {
            ra.addFlashAttribute("errorMsg", "제출하려면 모든 점수와 코멘트를 입력해야 합니다.");
            return "redirect:/eval/list?periodId=" + periodId;
        }
        ra.addFlashAttribute("errorMsg", "처리 중 오류가 발생했습니다.");
        return "redirect:/eval/list?periodId=" + periodId;
    }
}
