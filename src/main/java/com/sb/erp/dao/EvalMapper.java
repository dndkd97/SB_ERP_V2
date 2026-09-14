package com.sb.erp.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sb.erp.dto.EvalDto;

@Mapper
public interface EvalMapper {

	// ─── 평가 조회 ────────────────────────────────
	// 특정 회차에서 평가자가 평가할 대상 목록
	// (부서원 목록 + 이미 작성한 평가 상태 조인)
	// 대시보드 카드 UI용
	List<EvalDto> selectTargetsByEvaluator(@Param("periodId") int periodId, @Param("evaluatorId") int evaluatorId);

	// 평가 단건 조회 (수정 폼용)
	EvalDto selectByEvalId(@Param("evalId") int evalId);

	// 회차 내 특정 대상+평가자+유형 조합 조회 (중복 체크 및 기존 데이터 조회)
	EvalDto selectByPeriodTargetEvaluator(@Param("periodId") int periodId, @Param("targetEmpId") int targetEmpId,
			@Param("evaluatorId") int evaluatorId, @Param("evalType") String evalType);

	// 특정 사원이 받은 평가 이력 (emp/detail 임베드용)
	List<EvalDto> selectByTargetEmpId(int targetEmpId);

	// 회차별 전체 평가 목록 (관리자용)
	List<EvalDto> selectByPeriodId(int periodId);

	
	// ─── 통계 ───────────────────────────────────

	// 회차 내 평가자별 진행률 (제출 완료 수)
	int countSubmittedByEvaluator(@Param("periodId") int periodId, @Param("evaluatorId") int evaluatorId);
	
	// 회차의 평가 대상 사원 수 — 리포트 진행률 산정
	int countDistinctTargetsByPeriodId(@Param("periodId") int periodId);
	
	// 회차의 미제출 평가 건수 (DRAFT 상태 등 SUBMITTED가 아닌 것) — 회차 마감 검증용
	int countUnsubmittedByPeriod(@Param("periodId") int periodId);
	
	
	// ─── 평가 등록/수정 ──────────────────────────

	int insert(EvalDto dto);

	int update(EvalDto dto);
}
