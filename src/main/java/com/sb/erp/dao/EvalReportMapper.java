package com.sb.erp.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sb.erp.dto.EvalReportDto;
import com.sb.erp.dto.EvalReportSearchDto;

@Mapper
public interface EvalReportMapper {

	// ─── 조회 ─────────────────────────────────────

	// 회차별 리포트 목록 (관리자용)
	List<EvalReportDto> selectByPeriodId(@Param("periodId") int periodId,
	                                     @Param("comId") int comId);

	// 단건 리포트 상세 (관리자/본인 공용)
	EvalReportDto selectByReportId(@Param("reportId") int reportId,
	                               @Param("comId") int comId);

	// 특정 회차 + 특정 사원 리포트 (본인 조회, 존재 여부 판단 겸용)
	EvalReportDto selectByPeriodAndEmp(@Param("periodId") int periodId,
	                                   @Param("empId") int empId);

	// 특정 사원의 모든 리포트 이력 (본인 이력 화면, emp/detail 임베드용)
	List<EvalReportDto> selectByEmpId(@Param("empId") int empId);
	
	// 특정 사원의 가장 최근 리포트 1건 (emp/detail 임베드용)
	EvalReportDto selectLatestByEmpId(@Param("empId") int empId,
	                                  @Param("comId") int comId);

	// 회차의 리포트 생성 완료 건수
	int countByPeriodId(@Param("periodId") int periodId);
	
	// ─── 검색 + 페이징 (관리자 리포트 목록용) ───
	// 검색 조건 + 페이징된 리포트 목록.
	List<EvalReportDto> searchByPeriod(EvalReportSearchDto search);

	//검색 조건 만족하는 총 개수 (페이징 계산용)
	int countByPeriodSearch(EvalReportSearchDto search);

	
	// ─── 등록/수정 ────────────────────────────────
	// 리포트 신규 생성
	int insert(EvalReportDto dto);

	// 리포트 재생성 (기존 존재 시 update)
	int update(EvalReportDto dto);

	

	// ─── 리포트 생성 기반 집계 ─────────────────────

	// 회차 내 사원별 평가 집계 (SUBMITTED만).
	List<Map<String, Object>> selectAggregatesByPeriod(@Param("periodId") int periodId);
}
