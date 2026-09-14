package com.sb.erp.service;

import java.util.List;

import com.sb.erp.dto.EvalDto;

public interface EvalService {

	// ─── 조회 ────────────────────────────────

	// 대시보드 카드용: 회차별 내가 평가할 대상 목록
	List<EvalDto> selectTargetsByCurrentEvaluator(int periodId);

	// 평가 단건 조회 (수정 폼용)
	EvalDto selectByEvalId(int evalId);

	// 회차 내 전체 평가 (관리자용)
	List<EvalDto> selectByPeriodId(int periodId);

	// 사원 상세에서 임베드용: 특정 사원이 받은 평가 이력
	List<EvalDto> selectMyEvalHistory(); // 본인

	List<EvalDto> selectEvalHistoryByEmpId(int empId); // 관리자 조회용
	
	
	// ─── 통계 ────────────────────────────────

	// 회차 내 나(로그인 사용자)의 평가 진행률
	int countMySubmitted(int periodId);
	
	
	// ─── 등록 / 수정 ──────────────────────────

	// 임시 저장 (점수 일부 null 가능)
	// 반환값: 1=성공, -1=회차/대상 없음, -2=회차가 OPEN이 아님, -3=평가자 아님
	int saveDraft(EvalDto dto);

	// 최종 제출 (모든 점수 필수)
	// 반환값: 1=성공, -1=회차/대상 없음, -2=회차가 OPEN 아님, -3=평가자 아님, -4=점수/코멘트 누락
	int submit(EvalDto dto);
}
