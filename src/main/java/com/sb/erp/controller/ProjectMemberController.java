package com.sb.erp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sb.erp.dto.ProjectDto;
import com.sb.erp.dto.ProjectMemberDto;
import com.sb.erp.security.CustomUserDetails;
import com.sb.erp.service.ProjectMemberService;
import com.sb.erp.service.ProjectService;
import com.sb.erp.util.SecurityUtil;

@Controller
@RequestMapping("/proj")
public class ProjectMemberController {
	@Autowired ProjectMemberService service;
	@Autowired ProjectService projectService;
	
	@GetMapping("/proj_member")
	public String list(@RequestParam("pro_id") int proId, Model model,Authentication auth) {
		ProjectDto project = projectService.select(proId);
		SecurityUtil.checkComIdAccess(project.getComId());
		boolean isAdmin = SecurityUtil.isAdminOrRoot(auth);
		boolean isCreator = project.getEmpId() == SecurityUtil.getCurrentEmpId();

		if(!isAdmin && !isCreator){
		    throw new AccessDeniedException("접근 권한이 없습니다.");
		}
		 model.addAttribute("list", service.select(proId));
		 model.addAttribute("pro_id", proId);
		return "proj/proj_member";} //프로젝트 참여인원 조회
	
	  @PostMapping("/proj_member_create") 
	  public String insert(ProjectMemberDto dto,RedirectAttributes rttr, Authentication auth) { 
		    CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
		    int empId = user.getUser().getEmpId();

			ProjectDto project = projectService.select(dto.getProjectProId());
			SecurityUtil.checkComIdAccess(project.getComId());
			
			boolean isAdmin = SecurityUtil.isAdminOrRoot(auth);
			boolean isCreator = project.getEmpId() == empId;

			if (!isAdmin && !isCreator) {
				rttr.addFlashAttribute("result","프로젝트 생성자 또는 관리자만 멤버를 추가할 수 있습니다.");
				return "redirect:/proj/proj_member?pro_id=" + dto.getProjectProId();
			}
			
			try {
			    String result = "프로젝트 멤버 추가 실패";
			    if (service.insert(dto) > 0) {
			        result = "프로젝트 멤버 추가 성공";
			    }
			    rttr.addFlashAttribute("result", result);

			} catch (IllegalArgumentException e) {
			    rttr.addFlashAttribute("result", e.getMessage());
			}

		  return "redirect:/proj/proj_member?pro_id="+dto.getProjectProId(); } //프로젝트 멤버 추가
	 
	
	@GetMapping("/proj_member_delete") 
	public String delete(@RequestParam("pm_id")int pmId, @RequestParam("pro_id") int proId,
			RedirectAttributes rttr,Authentication auth) {
		
		   CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
		    int empId = user.getUser().getEmpId();
		ProjectDto project = projectService.select(proId);
		SecurityUtil.checkComIdAccess(project.getComId());
		boolean isAdmin = SecurityUtil.isAdminOrRoot(auth);
		boolean isCreator = project.getEmpId() == empId;

		if (!isAdmin && !isCreator) {
			rttr.addFlashAttribute("result", "프로젝트 생성자 또는 관리자만 멤버를 삭제할 수 있습니다.");
			return "redirect:/proj/proj_member?pro_id="+proId;
		}
		String result="프로젝트 멤버 삭제 실패";
		if(service.delete(pmId)>0) {result="프로젝트 멤버 삭제 성공";}
		rttr.addFlashAttribute("result",result);
		return "redirect:/proj/proj_member?pro_id="+proId;}//프로젝트 멤버 삭제

}
