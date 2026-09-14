package com.sb.erp.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeptTransferExecuteForm {
    private Integer deptId;
    private Integer comId;
    
    private String returnUrl;
    private List<EmployeeTransferItemForm> items;
 
    /** 화면 로드 시점에 조회했던 AI 추천 사유 — 그대로 dept_transfer_log.ai_reason 에 감사 기록 */
    private String aiReason;
 
    /** 화면 로드 시점의 결재문서 제목 요약 — dept_transfer_log.handover_snapshot 에 감사 기록 */
    private String snapshotText;
}
