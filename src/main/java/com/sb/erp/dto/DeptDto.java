package com.sb.erp.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DeptDto {
	private int deptId;
	private int comId;
	private int parentId;
	private String deptName;
	private String deptCode;
	private int depth;
	private int sortOrder;
	private Integer empId;
	private String createdAt;
	private String updatedAt;
	
	private String fullPath;
	private List<DeptDto> children;
	
	//부서 조회시 불러오는 추가 데이터
	private String leaderId;
	private String leaderName;
	private String parentName;
	private int empCount;
	
	//부서 상세 조회시 불러오는 추가 데이터
	private String leaderPosName;
	private String leaderEmpNo;

	public List<DeptDto> getChildren() {
		if(this.children == null) this.children = new ArrayList<>();
		return this.children;
	}
}
