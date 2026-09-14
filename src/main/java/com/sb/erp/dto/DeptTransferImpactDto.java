package com.sb.erp.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeptTransferImpactDto {
	private int deptId;
	private String deptName;
	private String deptCode;
	
	// 이관 대상 사원
	private List<EmpTransferDto> employees;
	// 사원 기준 미처리(WAI) 예약 
    private List<ResvImpactDto> reservations;
    // 사원 기준 미완료 결재라인 
    private List<ApprLineImpactDto> apprLines;
    // 사원이 기안한 진행중 결재문서
    private List<ApprDocImpactDto> apprDocs;
    // 사원이 기안한 진행중 결재문서 제목 요약
    private String snapshotText;
    // 부서 후보들
    private List<DeptDto> candidates;
    // 부서 추천 AI 내용
    private AiRecomDto aiRecom;
    
    
    public int getEmployeeCount() {
        return employees == null ? 0 : employees.size();
    }
 
    public int getReservationCount() {
        return reservations == null ? 0 : reservations.size();
    }
 
    public int getApprLineCount() {
        return apprLines == null ? 0 : apprLines.size();
    }
 
    public int getApprDocCount() {
        return apprDocs == null ? 0 : apprDocs.size();
    }
 
}
