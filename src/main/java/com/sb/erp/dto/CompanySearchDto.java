package com.sb.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompanySearchDto {
	private int comId;
	private String companyName;
}
