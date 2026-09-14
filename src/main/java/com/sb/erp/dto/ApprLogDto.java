package com.sb.erp.dto;

import lombok.Data;

@Data
public class ApprLogDto {
	private int logId;
	private int docId;
	private int oriEmpId;
	private int actEmpId;
	private int perEmpId;
	private String createdAt;
	
	// 프론트 출력용
	private String oriEmpName;
	private String actEmpName;
	private String perEmpName;
	
}
