package com.sb.erp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ResvSearchDto {
	private Integer comId;
	private Integer empId;
	private Integer resId;
	private Integer excludeRevId;
	
	private String status;
	private String resType;
	private String startDt;
	private String endDt;
	private String keyword;
	
	// 페이징
	private int pstartno = 1;
	private int onepagelist = 10;
	
	// 검색 조건이 비어있는지 확인 여부
	public boolean hasSearchCondition() {
		return (keyword != null && !keyword.isEmpty())
	            || (status != null && !status.isEmpty())
	            || (startDt != null && !startDt.isEmpty())
	            || (endDt != null && !endDt.isEmpty())
	            || (resType != null && !resType.isEmpty())
	            ;
	        // 검색 필드 추가될 때마다 여기에 || 조건 추가
	}
}
