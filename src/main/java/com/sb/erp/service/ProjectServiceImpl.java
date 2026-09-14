package com.sb.erp.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sb.erp.api.DiscordApi;
import com.sb.erp.api.OpenAiGpt;
import com.sb.erp.api.ReportApi;
import com.sb.erp.dao.ProjectMapper;
import com.sb.erp.dto.ProjectAnalysisDto;
import com.sb.erp.dto.ProjectDto;
import com.sb.erp.dto.ProjectSearchDto;
import com.sb.erp.dto.WeeklyReportDto;

@Service
public class ProjectServiceImpl implements ProjectService{
	@Autowired ProjectMapper dao;
	@Autowired private OpenAiGpt openAi;
	@Autowired private DiscordApi discordApi;
	@Autowired private ReportApi reportApi;

	//프로젝트 등록
	@Override public int insert(ProjectDto dto) {  return dao.insert(dto); }

	/*
	 * //상태별 조회
	 * 
	 * @Override public List<ProjectDto> selectByStatus(String proStatus) { return
	 * dao.selectByStatus(proStatus); }
	 * 
	 * //기간조회
	 * 
	 * @Override public List<ProjectDto> selectByPeriod(String startDate, String
	 * endDate) { return dao.selectByPeriod(startDate, endDate); }
	 * 
	 * //프로젝트명 검색
	 * 
	 * @Override public List<ProjectDto> searchByKeyword(String keyword) { return
	 * dao.searchByKeyword(keyword); }
	 */

	//프로젝트 상세보기
	@Override public ProjectDto select(int proId) {  return dao.select(proId); }
	
	//프로젝트 삭제
	@Transactional //세 쿼리 다 성공 → 트랜잭션 커밋 (전부 반영)
	@Override public int delete(int proId) {  
		 // 1. 태스크 삭제
		  dao.deleteTaskByProjectId(proId);

	     // 2. 프로젝트 멤버 삭제
		  dao.deleteMemberByProjectId(proId);

		 // 3. 프로젝트 삭제
		return  dao.deleteProject(proId); }
	
	//프로젝트 수정
	@Override public int edit(ProjectDto dto) {  return dao.update(dto); }
	
	//프로젝트 수정뷰
	@Override public ProjectDto editView(int proId) {  return dao.select(proId); }

	/*paging*/
	@Override public List<ProjectDto> selectAll(ProjectSearchDto search) {
		search.setPstartno((search.getPstartno()-1) * search.getOnepagelist());
		return dao.selectAll(search);
	}
	
    @Override public int selectCnt(ProjectSearchDto search) { return dao.selectCnt(search); }

	//Ai 결과 보고서
	@Override public ProjectAnalysisDto projectAnalysis(Integer proId) {
		ProjectAnalysisDto dto = dao.projectAnalysis(proId);
		if(dto==null) {return null;}
		long remainDays = ChronoUnit.DAYS.between(LocalDate.now(), dto.getEndDate());
		if(remainDays<0) {remainDays=0;}
		dto.setRemainDays(remainDays);
		return dto;
	}
	//디스코드 알람 전송
	@Override public String analyzeProject(Integer proId) {
		ProjectAnalysisDto dto = projectAnalysis(proId);
		if(dto==null) {return "프로젝트 정보를 찾을 수 없습니다.";}
		String result = openAi.analyzeProject(dto);
		if(result.contains("HIGH")) {discordApi.sendMessage("🚨 프로젝트 위험 감지 🚨\n\n" + result);}
				return result;
	}
	//주간 보고서
	@Override public WeeklyReportDto weeklyReport(Integer proId) {
	    WeeklyReportDto dto = dao.weeklyReport(proId);
	    if (dto == null) { return null; }
	    long remainDays = ChronoUnit.DAYS.between(LocalDate.now(), dto.getEndDate());
	    if (remainDays < 0) { remainDays = 0; }
	    dto.setRemainDays(remainDays);
	    return dto;
	} 
	
	//주간 보고서용으로 가져올 프로젝트들
	@Override public List<Integer> selectActiveProjectIds() {  return dao.selectActiveProjectIds(); }
	
}
