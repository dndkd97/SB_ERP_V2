package com.sb.erp.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class WeeklyReportDto {
	private Integer proId;
	private String projectName;
	
	//진행 현황
	private Integer totalTask;
	private Integer completedThisWeek;
	private Integer delayTaskCount;
	private Integer progressRate;
	private Integer doneTaskCount;
	private Integer notDoneTaskCount;
	
	//통계
	private Double avgTaskDays;
	private Integer avgDelayDays;
	
	//계산용
	private LocalDate endDate; 
	
	//ai에게 넘길 정보
	private long remainDays;
	
	//역할별 보고서
	private String reportRole;
}
