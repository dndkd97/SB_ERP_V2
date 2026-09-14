package com.sb.erp.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sb.erp.dto.EvalPeriodDto;
import com.sb.erp.dto.EvalPeriodSearchDto;

@Mapper
public interface EvalPeriodMapper { 
	
	
	
	// ─── 회차 조회 ────────────────────────────────────
	// 회차 목록 조회
	List<EvalPeriodDto> search(EvalPeriodSearchDto search);
	
	// 회차 단건 조회
	EvalPeriodDto selectByPeriodId(@Param("periodId") int periodId, @Param("comId") int comId);
	
	//상태별 개수
	Map<String, Integer> countByStatusAll(int comId);
	
	
	// ─── 회차 등록/수정 ────────────────────────────────
	// 회차 등록
	int insert(EvalPeriodDto dto);
	
	// 회차 정보 수정
	int update(EvalPeriodDto dto);

	// 상태 전환
	int updateStatus(@Param("periodId") int periodId, 
			@Param("periodStatus") String periodStatus,
			@Param("comId") int comId);
	
	
	// ─── 중복 확인 ────────────────────────────────────
	boolean isDuplicate(@Param("evalYear") int evalYear, 
			@Param("evalTerm") String evalTerm, @Param("comId") int comId);
	
	
	// ─── 하위 데이터 카운트 ────────────────────────────────────
	int countEvalsByPeriodId(int periodId);
	int countReportsByPeriodId(int periodId);
	
	
}
