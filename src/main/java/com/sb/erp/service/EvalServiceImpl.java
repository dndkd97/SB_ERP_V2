package com.sb.erp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sb.erp.dao.EvalMapper;
import com.sb.erp.dto.EvalDto;
import com.sb.erp.dto.EvalPeriodDto;
import com.sb.erp.util.SecurityUtil;

@Service
public class EvalServiceImpl implements EvalService {
	
	@Autowired EvalMapper dao;
	@Autowired EvalPeriodService evalPeriodService;

	// 5개 항목 가중치 (성과 40, 전문성 20, 협업 20, 태도 10, 성장 10)
	private static final BigDecimal W_PERFORMANCE = new BigDecimal("0.40");
	private static final BigDecimal W_EXPERTISE = new BigDecimal("0.20");
	private static final BigDecimal W_TEAMWORK = new BigDecimal("0.20");
	private static final BigDecimal W_ATTITUDE = new BigDecimal("0.10");
	private static final BigDecimal W_GROWTH = new BigDecimal("0.10");
	
	
	// ─── 조회 ────────────────────────────────
	@Override
	public List<EvalDto> selectTargetsByCurrentEvaluator(int periodId) {
		int evaluatorId = SecurityUtil.getCurrentEmpId();
		return dao.selectTargetsByEvaluator(periodId, evaluatorId);
	}

	@Override
	public EvalDto selectByEvalId(int evalId) {
		return dao.selectByEvalId(evalId);
	}

	@Override
	public List<EvalDto> selectByPeriodId(int periodId) {
		return dao.selectByPeriodId(periodId);
	}

	@Override
	public List<EvalDto> selectMyEvalHistory() {
		return dao.selectByTargetEmpId(SecurityUtil.getCurrentEmpId());
	}

	@Override
	public List<EvalDto> selectEvalHistoryByEmpId(int empId) {
		return dao.selectByTargetEmpId(empId);
	}

	// ─── 통계 ────────────────────────────────

	@Override
	public int countMySubmitted(int periodId) {
		return dao.countSubmittedByEvaluator(periodId, SecurityUtil.getCurrentEmpId());
	}

	// ─── 등록 / 수정 ──────────────────────────

	@Override
	public int saveDraft(EvalDto dto) {
		int validation = validateBase(dto);
		if (validation != 1)
			return validation;

		// 평가자 자동 세팅
		dto.setEvaluatorId(SecurityUtil.getCurrentEmpId());
		dto.setEvalType("LEADER");
		dto.setEvalStatus("DRAFT");

		// 가중 점수 계산 (일부만 채워졌어도 계산 가능한 만큼)
		dto.setWeightedScore(calculateWeightedScore(dto));

		return upsertEval(dto);
	}

	@Override
	public int submit(EvalDto dto) {
		int validation = validateBase(dto);
		if (validation != 1)
			return validation;

		// 제출 시 모든 점수 + 코멘트 필수
		if (dto.getScorePerformance() == null || dto.getScoreExpertise() == null || dto.getScoreTeamwork() == null
				|| dto.getScoreAttitude() == null || dto.getScoreGrowth() == null || isEmpty(dto.getStrengthComment())
				|| isEmpty(dto.getImprovementComment())) {
			return -4;
		}

		dto.setEvaluatorId(SecurityUtil.getCurrentEmpId());
		dto.setEvalType("LEADER");
		dto.setEvalStatus("SUBMITTED");
		dto.setWeightedScore(calculateWeightedScore(dto));

		return upsertEval(dto);
	}

	// ─── 내부 헬퍼 ────────────────────────────

	/* 기본 검증: 회차 존재 + OPEN 상태 + 대상 사원 확인. 
	 1: 통과 / -1: 회차 없음 / -2: 회차가 OPEN 아님 / -3: 평가 대상 없음 */
	
	private int validateBase(EvalDto dto) {
		EvalPeriodDto period = evalPeriodService.selectByPeriodId(dto.getPeriodId());
		if (period == null)
			return -1;
		if (!"OPEN".equals(period.getPeriodStatus()))
			return -2;
		return 1;
	}

	private boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}

	// 기존 평가가 있으면 update, 없으면 insert.
	private int upsertEval(EvalDto dto) {
		EvalDto existing = dao.selectByPeriodTargetEvaluator(dto.getPeriodId(), dto.getTargetEmpId(),
				dto.getEvaluatorId(), dto.getEvalType());

		if (existing != null) {
			dto.setEvalId(existing.getEvalId());
			return dao.update(dto);
		}
		return dao.insert(dto);
	}

	// 5개 점수의 가중 평균 계산. 하나라도 null이면 null 반환 (이 경우 계산 안 됨).
	private BigDecimal calculateWeightedScore(EvalDto dto) {
		if (dto.getScorePerformance() == null || dto.getScoreExpertise() == null || dto.getScoreTeamwork() == null
				|| dto.getScoreAttitude() == null || dto.getScoreGrowth() == null) {
			return null;
		}
		BigDecimal sum = W_PERFORMANCE.multiply(new BigDecimal(dto.getScorePerformance()))
				.add(W_EXPERTISE.multiply(new BigDecimal(dto.getScoreExpertise())))
				.add(W_TEAMWORK.multiply(new BigDecimal(dto.getScoreTeamwork())))
				.add(W_ATTITUDE.multiply(new BigDecimal(dto.getScoreAttitude())))
				.add(W_GROWTH.multiply(new BigDecimal(dto.getScoreGrowth())));
		return sum.setScale(2, RoundingMode.HALF_UP);
	}
}
