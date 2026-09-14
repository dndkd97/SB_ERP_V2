package com.sb.erp.dto;

import lombok.Data;

@Data
public class ApprLineDto {
	private int linId;
	private int docId;
	private int empId;
	private int linOrder;
	private String linStatus;
	private String linApproved;
	
	// 조회용
	private String empName;
	private String empStatus;
	private String posName; 
	private int posOrder;
}
