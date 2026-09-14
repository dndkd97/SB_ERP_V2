package com.sb.erp.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class TaskDto {
	private Integer taskId;
	private Integer proId;
	private Integer comId;
	private String taskName;
	private String taskDesc;
	private String taskStatus;
	private Integer pmId;
	private String pmName;
	private LocalDate taskStartDate;
	private LocalDate taskEndDate;
	private LocalDate actualStartDate; // 실제 착수일
	private LocalDate actualEndDate; //실제 완요일
	private LocalDate createdAt;
	private LocalDate updatedAt;
	private Integer parentTaskId;
	private String proName;
	private Integer depth; //후속작업 목록-트리 구조를 보여주기 위한 용도
	private String parentTaskStatus; //부모 태스크의 상태
	
	//프로젝트 기간
	private LocalDate startDate;
	private LocalDate endDate;
	
    // 지연 여부
    private boolean delayed;
    
}
