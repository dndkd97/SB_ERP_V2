package com.sb.erp.dto;

import lombok.Data;

@Data
public class ApprLineRequestDto {
	private int reqId;
	private int docId;
	private int linId;
	private int oriEmpId;
	private Integer newEmpId;
	private int reqEmpId;
	private Integer proEmpId;
	private String reqReason;
	private String reqStatus;
	private String createdAt;
	private String processedAt;
	
	// 프론트 출력용
	private String docTitle;
	private String oriEmpName;
	private String newEmpName;
	private String reqEmpName;
}
