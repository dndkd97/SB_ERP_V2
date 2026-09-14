package com.sb.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CompanyDto {
	private int comId;
	private String industryGrpCode;
	private String industryCode;
	private String comName;
	private String comCeo;
	private String bizNo;
	private String comTel;
	private String comLogo;
	private String createdAt;
	private String updatedAt;
	
	// list -> 임직원수
	private int empCount;
	
}
