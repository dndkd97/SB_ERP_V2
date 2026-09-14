package com.sb.erp.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ProjectMemberDto {
	private Integer pmId;
	private Integer projectProId;
	private Integer empId;
	private String memberRole;
	private LocalDate joinedAt;
	private String empName;
	private String proName;
	private String deptName;
	
}
