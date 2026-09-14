package com.sb.erp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeptSearchDto {
	private Integer comId;
	private Integer deptId;
	
	private String deptCode;
}
