package com.sb.erp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sb.erp.dto.PosDto;
import com.sb.erp.service.PosService;

@Controller
public class PosController {

	@Autowired
	PosService posService;

	// ─── 목록 조회 ────────────────────────
	@GetMapping("/pos/list")
	public String list(Model model) {
		List<PosDto> posList = posService.selectAll();
		model.addAttribute("posList", posList);
		return "pos/list";
	}

	// ─── 등록 폼 ──────────────────────────
	@GetMapping("/pos/add")
	public String addForm() { return "pos/add"; }

	// ─── 등록 처리 ────────────────────────
	@PostMapping("/pos/add")
	public String addProcess(PosDto dto) {
		posService.insert(dto);
		return "redirect:/pos/list";
	}

	// ─── 수정 폼 ──────────────────────────
	@GetMapping("/pos/edit")
	public String editForm(@RequestParam int posId, Model model) {
		PosDto pos = posService.selectOneById(posId);
		if (pos == null) {
			return "redirect:/pos/list";
		}
		model.addAttribute("pos", pos);
		return "pos/edit";
	}

	// ─── 수정 처리 ────────────────────────
	@PostMapping("/pos/edit")
	public String editProcess(PosDto dto) {
		posService.update(dto);
		return "redirect:/pos/list";
	}

	// ─── 삭제 처리 ────────────────────────
	@PostMapping("/pos/delete")
	public String delete(@RequestParam int posId, RedirectAttributes ra) {
		int result = posService.delete(posId);
		if (result == -1) {
			ra.addFlashAttribute("errorMsg", "이 직급을 사용중인 사원이 있습니다. 사원의 직급을 먼저 변경한 후 삭제해주세요.");
		} else if (result == 0) {
			ra.addFlashAttribute("errorMsg", "삭제할 직급을 찾을 수 없습니다.");
		}
		return "redirect:/pos/list";
	}

	// ─── 중복 검사 ─────────────────
	@GetMapping("/pos/checkCode")
	@ResponseBody
	public Map<String, Object> checkCode(@RequestParam String posCode,
			@RequestParam(required = false) Integer excludePosId) {
		Map<String, Object> result = new HashMap<>();
		result.put("duplicate", posService.isPosCodeDuplicate(posCode, excludePosId));
		return result;
	}
}