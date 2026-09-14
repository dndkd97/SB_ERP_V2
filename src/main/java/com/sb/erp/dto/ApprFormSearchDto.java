package com.sb.erp.dto;

import lombok.Data;

@Data
public class ApprFormSearchDto {
	private String keyword; 
	private Integer comId;
	private String comName;
	private Boolean forStatus;
	
	// 페이징 기능
	private int pstartno = 1;
	private int onepagelist = 10;
	
	// 검색 조건이 비어있는지 확인 여부
	public boolean hasSearchCondition() {
        return (keyword != null && !keyword.isEmpty())
            || (comName != null && !comName.isEmpty())
            || forStatus != null;
        // 검색 필드 추가될 때마다 여기에 || 조건 추가
    }
	
}
