package com.sb.erp.service;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.sb.erp.dto.ProjectAnalysisDto;
import com.sb.erp.dto.ProjectDto;
import com.sb.erp.dto.ProjectSearchDto;
import com.sb.erp.dto.WeeklyReportDto;

public interface ProjectService {
	//프로젝트 등록
	public int insert(ProjectDto dto);
	
	/*
	 * //프로젝트 상태별 리스트 public List<ProjectDto> selectByStatus(String proStatus);
	 * 
	 * //기간조회 public List<ProjectDto> selectByPeriod(String startDate, String
	 * endDate);
	 * 
	 * //프로젝트명 조회 public List<ProjectDto> searchByKeyword(@Param("keyword") String
	 * keyword);
	 */
	
	//프로젝트 상세보기
	public ProjectDto select(int proId);
	
	//프로젝트 삭제
	public int delete(int proId);
	
	//프로젝트 수정
	public int edit(ProjectDto dto);
	
	//프로젝트 수정뷰
	public ProjectDto editView(int proId);
	
	/* paging */
	public List<ProjectDto> selectAll(ProjectSearchDto search);
	public int selectCnt(ProjectSearchDto search);

	//Ai 분석 결과
	public ProjectAnalysisDto projectAnalysis(Integer proId);
	public String analyzeProject(Integer proId);
	
	//주간 보고서용-팀장용
	public WeeklyReportDto weeklyReport(Integer proId);
	
	//주간 보고서용으로 가져올 프로젝트들
	public List<Integer>selectActiveProjectIds();
}
