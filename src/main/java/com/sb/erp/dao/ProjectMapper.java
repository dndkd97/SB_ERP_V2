package com.sb.erp.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sb.erp.dto.MyWeeklyReportDto;
import com.sb.erp.dto.ProjectAnalysisDto;
import com.sb.erp.dto.ProjectDto;
import com.sb.erp.dto.ProjectSearchDto;
import com.sb.erp.dto.WeeklyReportDto;

@Mapper
public interface ProjectMapper {
	// 프로젝트 등록
	public int insert(ProjectDto dto);
	
	/*
	 * //프로젝트 상태별 리스트 public List<ProjectDto> selectByStatus(String proStatus);
	 * 
	 * //기간조회 public List<ProjectDto> selectByPeriod(@Param("startDate") String
	 * startDate, @Param("endDate") String endDate);
	 * 
	 * //프로젝트명 검색 public List<ProjectDto> searchByKeyword(@Param("keyword") String
	 * keyword);
	 */
	
	//프로젝트 상세보기
	public ProjectDto select(int proId);
	
	//프로젝트 삭제
	public int deleteTaskByProjectId(int proId);
	public int deleteMemberByProjectId(int proId);
	public int deleteProject(int proId);
	
	//프로젝트 수정
	public int update(ProjectDto dto);
	
	/* paging */
	public List<ProjectDto> selectAll(ProjectSearchDto search);
	public int selectCnt(ProjectSearchDto search);

	
	//Ai 프로젝트 분석용
	public ProjectAnalysisDto projectAnalysis(Integer proId);
	
	//주간 보고서용-팀장용
	public WeeklyReportDto weeklyReport(Integer proId);
	
	//주간 보고서용으로 가져올 프로젝트들
	public List<Integer>selectActiveProjectIds();
	
}