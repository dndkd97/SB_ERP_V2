package com.sb.erp.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.sb.erp.dao.EvalMapper;
import com.sb.erp.dao.EvalPeriodMapper;
import com.sb.erp.dto.EvalPeriodDto;
import com.sb.erp.dto.EvalPeriodSearchDto;
import com.sb.erp.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class EvalPeriodServiceImpl implements EvalPeriodService {

    private final EvalPeriodMapper dao;
    private final EvalReportBatchService evalReportBatchService;
    private final EvalMapper evalMapper;

	// ─── 회차 조회 ────────────────────────────────────
	@Override
	public List<EvalPeriodDto> search(EvalPeriodSearchDto search) {
		search.setComId(SecurityUtil.getCurrentComId());
		return dao.search(search);
	}

	@Override
	public EvalPeriodDto selectByPeriodId(int periodId) {
		return dao.selectByPeriodId(periodId, SecurityUtil.getCurrentComId());
	}

	@Override
	public Map<String, Integer> countByStatusAll() {
		return dao.countByStatusAll(SecurityUtil.getCurrentComId());
	}

	// ─── 회차 등록/수정 ────────────────────────────────
	@Override
	public int insert(EvalPeriodDto dto) {
		dto.setComId(SecurityUtil.getCurrentComId());
		return dao.insert(dto);
	}

	@Override
	public int update(EvalPeriodDto dto) {
		dto.setComId(SecurityUtil.getCurrentComId());
		return dao.update(dto);
	}

	// 이하 회차 상태 업데이트
	@Override
	public int openPeriod(int periodId) {
	    EvalPeriodDto period = selectByPeriodId(periodId);
	    if (period == null) {
	        System.err.println("[EvalPeriod] 개시 실패(-1): 회차 없음 periodId=" + periodId);
	        return -1;
	    }

	    if (!"READY".equals(period.getPeriodStatus())) {
	        System.err.println("[EvalPeriod] 개시 실패(-2): 허용되지 않은 상태 status=" 
	                + period.getPeriodStatus() + " (READY만 가능) periodId=" + periodId);
	        return -2;
	    }

	    return dao.updateStatus(periodId, "OPEN", SecurityUtil.getCurrentComId());
	}

	@Override
	public int closePeriod(int periodId) {
	    EvalPeriodDto period = selectByPeriodId(periodId);
	    if (period == null) {
	        System.err.println("[EvalPeriod] 마감 실패(-1): 회차 없음 periodId=" + periodId);
	        return -1;
	    }

	    if (!"OPEN".equals(period.getPeriodStatus())) {
	        System.err.println("[EvalPeriod] 마감 실패(-2): 허용되지 않은 상태 status=" 
	                + period.getPeriodStatus() + " (OPEN만 가능) periodId=" + periodId);
	        return -2;
	    }

	    int unsubmitted = evalMapper.countUnsubmittedByPeriod(periodId);
	    if (unsubmitted > 0) {
	        System.err.println("[EvalPeriod] 마감 실패(-3): 미제출 평가 " + unsubmitted 
	                + "건 존재 periodId=" + periodId);
	        return -3;
	    }

	    return dao.updateStatus(periodId, "CLOSED", SecurityUtil.getCurrentComId());
	}

	@Override
	@Transactional
	public int reportPeriod(int periodId) {
	    EvalPeriodDto period = selectByPeriodId(periodId);
	    if (period == null) {
	        System.err.println("[EvalPeriod] 리포트 개시 실패(-1): 회차 없음 periodId=" + periodId);
	        return -1;
	    }

	    String status = period.getPeriodStatus();
	 // 진입 허용 상태:
	 // - CLOSED: 최초 발행 (평가 마감 직후)
	 // - REPORTING_FAILED: 실패 후 재시도
	 // - REPORTED: 완료 리포트 전체 재생성 (프롬프트 튜닝, 데이터 수정 후 등)
	 // REPORTING은 배치 진행 중이므로 중복 진입 금지.
	 if (!"CLOSED".equals(status)
	     && !"REPORTING_FAILED".equals(status)
	     && !"REPORTED".equals(status)) {
	     System.err.println("[EvalPeriod] 리포트 개시 실패(-2): 허용되지 않은 상태 status=" + status 
	             + " periodId=" + periodId);
	     return -2;
	 }

		int comId = SecurityUtil.getCurrentComId();

		// 상태를 즉시 REPORTING으로 전환 (이 트랜잭션이 커밋되면 확정)
		int result = dao.updateStatus(periodId, "REPORTING", comId);
		if (result != 1) {
			return result;
		}

		// ⭐ 배치는 트랜잭션 커밋 후 실행되어야 함
		// - 커밋 전에 배치가 시작되면: 롤백 시 상태 불일치 + 배치가 존재하지 않는 REPORTING 회차 처리
		// - afterCommit 훅으로 커밋 확정 후에만 배치 개시
		// - SecurityContext는 async 스레드에서 못 쓰니 comId를 파라미터로 캡처
		final int finalComId = comId;
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				evalReportBatchService.runInBackground(periodId, finalComId);
			}
		});

		return 1;
	}

	// ─── 중복 확인 ────────────────────────────────────
	@Override
	public boolean isDuplicate(int evalYear, String evalTerm) {
		return dao.isDuplicate(evalYear, evalTerm, SecurityUtil.getCurrentComId());
	}

	// ─── 하위 데이터 카운트 ──────────────────────────────
	@Override
	public int countEvalsByPeriodId(int periodId) {
		return dao.countEvalsByPeriodId(periodId);
	}

	@Override
	public int countReportsByPeriodId(int periodId) {
		return dao.countReportsByPeriodId(periodId);
	}

}
