package com.sb.erp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class DeptTransferLogSearchDto {
    private Integer originDeptId; //원부서
    private Integer targetDeptId; //대상부서
    // "Y" | "N" | null(전체)
    private String aiRecommended; // ai 제안여부   
    private String dateFrom;      // 처리시작일자
    private String dateTo;        // 처리종료일자
    
    // 페이지 네이션
    private int pstartno = 1;
    private int onepagelist = 10;
}
 
