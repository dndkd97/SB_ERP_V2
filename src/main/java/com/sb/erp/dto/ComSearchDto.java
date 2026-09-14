package com.sb.erp.dto;

import lombok.Data;

@Data
public class ComSearchDto {
	private String keyword;
	private String industryGrpCode;
	private String industryCode;
	
	private int onepagelist = 10;
	private int pstartno = 1;
	
	// 검색 조건이 비어있는지 확인 여부
	public boolean hasSearchCondition() {
        return (keyword != null && !keyword.isEmpty())
            || (industryGrpCode != null && !industryGrpCode.isEmpty())
            || (industryCode != null && !industryCode.isEmpty());
        // 검색 필드 추가될 때마다 여기에 || 조건 추가
    }
}
