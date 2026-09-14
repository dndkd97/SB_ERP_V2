package com.sb.erp.dto;

import lombok.Setter;
import lombok.ToString;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class DeptTransferLogDto {
	private Integer logId;
	private Integer comId;
	//원부서
	private Integer originDeptId;
	private String originDeptName;
	//새부서
	private Integer targetDeptId;
	private String targetDeptName;
	//이관된 사원정보
	private Integer empId;
	private String empNo;
    private String empName;
	//ai 관련 정보
    private String aiRecommended;
	private String aiReason;
	private String handoverSnapshot;
	//이관 시킨 사원 정보 및 일자
	private Integer createdBy;
    private String createdByName;
    private String createdAt;
	
}
