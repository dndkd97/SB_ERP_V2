package com.sb.erp.service;

import java.util.List;

import com.sb.erp.dto.EvalReportDto;
import com.sb.erp.dto.EvalReportSearchDto;

public interface EvalReportService {

	// ─── 조회 (관리자용) ───
	List<EvalReportDto> selectByPeriodId(int periodId);
	EvalReportDto selectByReportId(int reportId);
	int countByPeriodId(int periodId);

	// ─── 조회 (본인용) ───
	EvalReportDto selectMyByPeriod(int periodId);
	List<EvalReportDto> selectMyAll();
	
	// 특정 사원의 최근 리포트 1건 (emp/detail 임베드용)
	// - 관리자/본인 공용. 컨트롤러에서 이미 접근 권한을 검증한 뒤 호출한다는 전제.
	// - 리포트가 아직 없으면 null 리턴.
	EvalReportDto selectLatestByEmpId(int empId);
	
	// ─── 검색 + 페이징 ───
	List<EvalReportDto> searchByPeriod(EvalReportSearchDto search);
	int countByPeriodSearch(EvalReportSearchDto search);

	// ─── 생성 ───
	// 회차 전체 리포트 일괄 생성/재생성
	// 반환 규약: 1=성공, -1=회차없음, -2=상태오류(CLOSED/REPORTED만 허용), -3=평가없음
	int generateReports(int periodId);

	// 특정 사원 리포트 개별 재생성
	// 반환 규약: 1=성공, -1=회차없음, -2=상태오류, -3=평가없음
	int regenerateReport(int periodId, int empId);
}
