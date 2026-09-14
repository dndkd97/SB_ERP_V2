package com.sb.erp.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ProjectAnalysisDto {
	private Integer proId;
	private String projectName;
	
	//계산용
	private LocalDate endDate; 
	
	//태스크 통계
	private Integer totalTask;
	private Integer todoCount;
	private Integer doingCount;
	private Integer doneCount;
	private Integer delayCount;
	
	//계산 결과
	private Integer progressRate;
	private long remainDays;

}
