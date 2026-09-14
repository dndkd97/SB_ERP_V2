package com.sb.erp.dto;

import java.time.LocalDate;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class ProjectDto {
	private Integer proId;
	private Integer comId;
	private Integer empId;
	private String proStatus;
	private String proName;
	private String proDesc;
	
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalDate actualStartDate; //실제 착수일
	private LocalDate actualEndDate; //실제 완료일

	private LocalDate createdAt;
	private LocalDate updatedAt;
	
	private String empName;
	private Integer memberCnt; //프로젝트 멤버 인원 db값 존재하지않는 컬럼

	
	
}
