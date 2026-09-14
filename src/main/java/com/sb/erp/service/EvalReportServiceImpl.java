package com.sb.erp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sb.erp.api.OpenAiClient;
import com.sb.erp.dto.openai.ChatMessage;
import com.sb.erp.dto.openai.ReportContent;
import com.sb.erp.dao.EvalPeriodMapper;
import com.sb.erp.dao.EvalReportMapper;
import com.sb.erp.dto.EvalPeriodDto;
import com.sb.erp.dto.EvalReportDto;
import com.sb.erp.dto.EvalReportSearchDto;
import com.sb.erp.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class EvalReportServiceImpl implements EvalReportService {

    private final EvalReportMapper dao;
    private final EvalPeriodMapper evalPeriodMapper;
    private final OpenAiClient openAiClient;

    // 실제 GPT 사용 시 모델 이름
    private static final String REAL_MODEL_NAME = "gpt-4o-mini";

	// 가중치 (합계 1.00)
	private static final BigDecimal W_PERFORMANCE = new BigDecimal("0.40");
	private static final BigDecimal W_EXPERTISE = new BigDecimal("0.20");
	private static final BigDecimal W_TEAMWORK = new BigDecimal("0.20");
	private static final BigDecimal W_ATTITUDE = new BigDecimal("0.10");
	private static final BigDecimal W_GROWTH = new BigDecimal("0.10");

	// Mock 리포트 모델 표기 
	private static final String MOCK_MODEL_NAME = "mock-analyzer-v1";
	
	// GPT 응답이 사용 가능한지 검증함. 
	// 요약문이 비어있지 않은가, 감성 3개가 모두 있고 합이 대략 100.0인가

	private boolean isValidGptResult(ReportContent r) {
		if (r.summary() == null || r.summary().isBlank())
			return false;
		if (r.sentimentPositive() == null || r.sentimentNeutral() == null || r.sentimentNegative() == null)
			return false;

		// 합계 검증 (ABS(합계-100.0) < 0.01) - DB는 백분율 스케일
		BigDecimal sum = r.sentimentPositive().add(r.sentimentNeutral()).add(r.sentimentNegative());
		BigDecimal diff = sum.subtract(new BigDecimal("100.0")).abs();
		if (diff.compareTo(new BigDecimal("0.01")) >= 0) {
			System.err.println("[EvalReport] GPT 감성 합계 이상: " + sum);
			return false;
		}

		return true;
	}

	// ─── 조회 ─────────────────────────────────────

	@Override
	public List<EvalReportDto> selectByPeriodId(int periodId) {
		return dao.selectByPeriodId(periodId, SecurityUtil.getCurrentComId());
	}

	@Override
	public EvalReportDto selectByReportId(int reportId) {
		return dao.selectByReportId(reportId, SecurityUtil.getCurrentComId());
	}

	@Override
	public int countByPeriodId(int periodId) {
		return dao.countByPeriodId(periodId);
	}

	@Override
	public EvalReportDto selectMyByPeriod(int periodId) {
		return dao.selectByPeriodAndEmp(periodId, SecurityUtil.getCurrentEmpId());
	}

	@Override
	public List<EvalReportDto> selectMyAll() {
		return dao.selectByEmpId(SecurityUtil.getCurrentEmpId());
	}
	
	@Override
	public EvalReportDto selectLatestByEmpId(int empId) {
	    return dao.selectLatestByEmpId(empId, SecurityUtil.getCurrentComId());
	}
	
	@Override
	public List<EvalReportDto> searchByPeriod(EvalReportSearchDto search) {
	    search.setComId(SecurityUtil.getCurrentComId());
	    return dao.searchByPeriod(search);
	}

	@Override
	public int countByPeriodSearch(EvalReportSearchDto search) {
	    search.setComId(SecurityUtil.getCurrentComId());
	    return dao.countByPeriodSearch(search);
	}
	
	

	// ─── 생성/재생성 ───────────────────────────────
	@Override
	public int generateReports(int periodId) {
		// 회차 소유/존재 확인 (comId 격리 포함)
		EvalPeriodDto period = evalPeriodMapper.selectByPeriodId(periodId, SecurityUtil.getCurrentComId());
		if (period == null) {
		    System.err.println("[EvalReport] 실패(-1): 회차 없음 periodId=" + periodId);
		    return -1;
		}

		String status = period.getPeriodStatus();
		if (!"REPORTING".equals(status) && !"REPORTED".equals(status)) {
		    System.err.println("[EvalReport] 실패(-2): 허용되지 않은 상태 status=" + status 
		            + " (REPORTING/REPORTED만 가능) periodId=" + periodId);
		    return -2;
		}

		List<Map<String, Object>> aggregates = dao.selectAggregatesByPeriod(periodId);
		if (aggregates == null || aggregates.isEmpty()) {
		    System.err.println("[EvalReport] 실패(-3): 집계 대상 없음 (SUBMITTED 상태 평가 부재) periodId=" + periodId);
		    return -3;
		}

		// 사원별 리포트 생성 (기존 존재 시 update)
		for (Map<String, Object> agg : aggregates) {
			EvalReportDto report = buildReportFromAggregate(periodId, agg);

			// 기존 리포트 존재 여부 확인 (upsert 분기)
			EvalReportDto existing = dao.selectByPeriodAndEmp(periodId, report.getEmpId());
			if (existing == null) {
				dao.insert(report);
			} else {
				dao.update(report);
			}
		}
		return 1;
	}

	@Override
	public int regenerateReport(int periodId, int empId) {
		EvalPeriodDto period = evalPeriodMapper.selectByPeriodId(periodId, SecurityUtil.getCurrentComId());
		if (period == null) {
		    System.err.println("[EvalReport] 재생성 실패(-1): 회차 없음 periodId=" + periodId);
		    return -1;
		}

		String status = period.getPeriodStatus();
		// REPORTED만 허용:
		// - CLOSED/REPORTING/REPORTING_FAILED는 배치 시스템이 관장 (개별 우회 금지)
		// - 개별 재생성은 완료된 리포트를 수정하는 도구
		if (!"REPORTED".equals(status)) {
		    System.err.println("[EvalReport] 재생성 실패(-2): 허용되지 않은 상태 status=" + status 
		            + " (REPORTED만 가능) periodId=" + periodId);
		    return -2;
		}
		
		// 해당 사원의 집계만 찾기 (전체 집계 조회 후 필터링)
		List<Map<String, Object>> aggregates = dao.selectAggregatesByPeriod(periodId);
		for (Map<String, Object> agg : aggregates) {
			int aggEmpId = toInt(agg.get("empId"));
			if (aggEmpId == empId) {
				EvalReportDto report = buildReportFromAggregate(periodId, agg);
				EvalReportDto existing = dao.selectByPeriodAndEmp(periodId, empId);
				if (existing == null) {
					dao.insert(report);
				} else {
					dao.update(report);
				}
				return 1;
			}
		}
		
		// 실무에서는 System.out/err 대신 SLF4J를 쓴다고 함(참고)
		System.err.println("[EvalReport] 재생성 실패(-3): 해당 사원의 평가가 없음 periodId=" + periodId 
		        + " empId=" + empId);
		return -3;
	}

	// ─── 리포트 생성 로직 ───
	private EvalReportDto buildReportFromAggregate(int periodId, Map<String, Object> agg) {
		EvalReportDto dto = new EvalReportDto();
		dto.setPeriodId(periodId);
		dto.setEmpId(toInt(agg.get("empId")));

		// ─── 1) 숫자 계산 부분 (기존과 동일, GPT와 무관) ───

		BigDecimal avgP = toBigDecimal(agg.get("avgPerformance"));
		BigDecimal avgE = toBigDecimal(agg.get("avgExpertise"));
		BigDecimal avgT = toBigDecimal(agg.get("avgTeamwork"));
		BigDecimal avgA = toBigDecimal(agg.get("avgAttitude"));
		BigDecimal avgG = toBigDecimal(agg.get("avgGrowth"));

		dto.setAvgPerformance(avgP);
		dto.setAvgExpertise(avgE);
		dto.setAvgTeamwork(avgT);
		dto.setAvgAttitude(avgA);
		dto.setAvgGrowth(avgG);

		BigDecimal overall = avgP.multiply(W_PERFORMANCE).add(avgE.multiply(W_EXPERTISE)).add(avgT.multiply(W_TEAMWORK))
				.add(avgA.multiply(W_ATTITUDE)).add(avgG.multiply(W_GROWTH)).setScale(2, RoundingMode.HALF_UP);
		dto.setOverallScore(overall);
		dto.setGrade(calcGrade(overall));

		// ─── 2) GPT 호출 시도 (실패 시 Mock fallback) ───

		String empName = (String) agg.get("empName");
		String posName = (String) agg.get("posName");
		String deptName = (String) agg.get("deptName");
		String strengths = (String) agg.get("allStrengthComments");
		String improvements = (String) agg.get("allImprovementComments");
		int evalCount = toInt(agg.get("evalCount"));

		// 프롬프트 조립
		List<ChatMessage> messages = List.of(ChatMessage.system(EvalReportPrompts.SYSTEM_INSTRUCTION), ChatMessage
				.user(EvalReportPrompts.buildUserPrompt(empName, posName, deptName, strengths, improvements)));

		// GPT 호출 — 실패해도 예외 안 던짐, null 리턴
		ReportContent gptResult = openAiClient.generateReport(messages);

		if (gptResult != null && isValidGptResult(gptResult)) {
			// ✅ GPT 성공 경로
			dto.setAiSummary(gptResult.summary());
			dto.setSentimentPositive(gptResult.sentimentPositive());
			dto.setSentimentNeutral(gptResult.sentimentNeutral());
			dto.setSentimentNegative(gptResult.sentimentNegative());
			dto.setModelName(REAL_MODEL_NAME);
			System.out.println("[EvalReport] GPT 성공 empId=" + dto.getEmpId());
		} else {
			// ⚠️ GPT 실패 → Mock fallback
			BigDecimal[] sentiment = mockSentiment(overall);
			dto.setSentimentPositive(sentiment[0]);
			dto.setSentimentNeutral(sentiment[1]);
			dto.setSentimentNegative(sentiment[2]);
			dto.setAiSummary(mockSummary(empName, overall, dto.getGrade(), strengths, improvements, evalCount));
			dto.setModelName(MOCK_MODEL_NAME);
			System.err.println("[EvalReport] GPT 실패 → Mock 사용 empId=" + dto.getEmpId());
		}

		// ─── 3) sentimentLabel은 자바가 판단 (감성 3개 중 최대값 기반) ───

		dto.setSentimentLabel(
				labelFromSentiment(dto.getSentimentPositive(), dto.getSentimentNeutral(), dto.getSentimentNegative()));

		return dto;
	}

	// 종합 점수 → 등급 매핑
	// S: 4.5+, A: 4.0+, B: 3.5+, C: 3.0+, D: 그 이하
	private String calcGrade(BigDecimal overall) {
		if (overall.compareTo(new BigDecimal("4.5")) >= 0) {
			return "S";
		}
		if (overall.compareTo(new BigDecimal("4.0")) >= 0) {
			return "A";
		}
		if (overall.compareTo(new BigDecimal("3.5")) >= 0) {
			return "B";
		}
		if (overall.compareTo(new BigDecimal("3.0")) >= 0) {
			return "C";
		}
		return "D";
	}

	// 종합 점수 기반 mock 감성 분포 (합계 정확히 100.00, ck_report_sent_sum 통과)
	// DB 저장 규약: 백분율(0~100), 합계 100
	private BigDecimal[] mockSentiment(BigDecimal overall) {
	    double s = overall.doubleValue();
	    double pos, neu, neg;
	    if (s >= 4.0) {
	        pos = 70.0;
	        neu = 25.0;
	        neg = 5.0;
	    } else if (s >= 3.5) {
	        pos = 50.0;
	        neu = 35.0;
	        neg = 15.0;
	    } else if (s >= 3.0) {
	        pos = 30.0;
	        neu = 45.0;
	        neg = 25.0;
	    } else if (s >= 2.5) {
	        pos = 15.0;
	        neu = 40.0;
	        neg = 45.0;
	    } else {
	        pos = 5.0;
	        neu = 30.0;
	        neg = 65.0;
	    }

	    BigDecimal p = new BigDecimal(pos).setScale(2, RoundingMode.HALF_UP);
	    BigDecimal ne = new BigDecimal(neu).setScale(2, RoundingMode.HALF_UP);

	    // 합계 강제로 100.00 맞추기 (negative = 100 - pos - neu)
	    BigDecimal ng = new BigDecimal("100.00").subtract(p).subtract(ne).setScale(2, RoundingMode.HALF_UP);
	    return new BigDecimal[] { p, ne, ng };
	}

	private String labelFromSentiment(BigDecimal p, BigDecimal ne, BigDecimal ng) {
		if (p.compareTo(ne) >= 0 && p.compareTo(ng) >= 0)
			return "POSITIVE";
		if (ng.compareTo(p) > 0 && ng.compareTo(ne) > 0)
			return "NEGATIVE";
		return "NEUTRAL";
	}

	// Mock 요약문 (※API 연동 전 템플릿)
	private String mockSummary(String empName, BigDecimal overall, String grade, String strengths, String improvements,
			int evalCount) {

		StringBuilder sb = new StringBuilder();
		sb.append("[Mock 요약] ").append(empName == null ? "대상 사원" : empName).append("님은 총 ").append(evalCount)
				.append("건의 평가를 받았으며, ").append("종합 점수 ").append(overall.toPlainString()).append("점(").append(grade)
				.append(" 등급)을 획득했습니다.\n\n");

		sb.append("■ 주요 강점\n");
		if (strengths != null && !strengths.isBlank()) {
			sb.append(truncate(strengths, 300)).append("\n\n");
		} else {
			sb.append("(수집된 강점 코멘트 없음)\n\n");
		}

		sb.append("■ 개선 제안\n");
		if (improvements != null && !improvements.isBlank()) {
			sb.append(truncate(improvements, 300)).append("\n\n");
		} else {
			sb.append("(수집된 개선 코멘트 없음)\n\n");
		}

		sb.append("※ 본 요약은 OpenAI 연동 전 임시 mock 데이터입니다.");
		return sb.toString();
	}

	private String truncate(String s, int max) {
		if (s == null) {
			return "";
		}
		return s.length() <= max ? s : s.substring(0, max) + "...";
	}

	private int toInt(Object v) {
		if (v == null) {
			return 0;
		}
		if (v instanceof Number) {
			return ((Number) v).intValue();
		}
		return Integer.parseInt(v.toString());
	}

	private BigDecimal toBigDecimal(Object v) {
		if (v == null) {
			return BigDecimal.ZERO;
		}
		if (v instanceof BigDecimal) {
			return (BigDecimal) v;
		}
		if (v instanceof Number) {
			return new BigDecimal(v.toString());
		}
		return new BigDecimal(v.toString());
	}
}
