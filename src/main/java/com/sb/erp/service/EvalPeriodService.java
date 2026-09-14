package com.sb.erp.service;

import java.util.List;
import java.util.Map;

import com.sb.erp.dto.EvalPeriodDto;
import com.sb.erp.dto.EvalPeriodSearchDto;

public interface EvalPeriodService {

	// ─── 회차 조회 ────────────────────────────────────
	// 회차 목록 조회
	List<EvalPeriodDto> search(EvalPeriodSearchDto search);

	// 회차 단건 조회
	EvalPeriodDto selectByPeriodId(int periodId);

	// 상태별 개수
	Map<String, Integer> countByStatusAll();

	
	// ─── 회차 등록/수정 ────────────────────────────────
	// 회차 등록
	int insert(EvalPeriodDto dto);

	// 회차 정보 수정
	int update(EvalPeriodDto dto);

	// 상태 전환
	int openPeriod(int periodId);
    int closePeriod(int periodId);
    int reportPeriod(int periodId);

    
	// ─── 중복 확인 ────────────────────────────────────
	boolean isDuplicate(int evalYear, String evalTerm);

	
	// ─── 하위 데이터 카운트 ──────────────────────────────
	int countEvalsByPeriodId(int periodId);

	int countReportsByPeriodId(int periodId);

}
