package com.sb.erp.dto;

import java.util.List; 
import lombok.Data;

@Data
public class AuthUserDto {
	
	private String empNo;
	private String empName;
	private String empEmail;
	private String empPass;
	private String posName;
	private String comName;
	private List<AuthDto> authList;
	
	private int empId;
	private int comId;
	private int deptId;
	private int posId;
	
}