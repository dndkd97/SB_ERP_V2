package com.sb.erp.dto;

import lombok.Data;

@Data
public class ApprDocInitResponseDto {
	private int empId;
	private int comId;
	private int deptId;
	private String empName;
	private String posName;
	private String deptName;
	private String comName;
	private int posOrder;
}
