package com.sb.erp.dto;

import lombok.Data;

/**
 * AI 리포트 목록 검색 조건.
 * - periodId: 필수 (특정 회차의 리포트만)
 * - deptId: 부서 필터 (선택)
 * - keyword: 사원명 / 사번 검색 (선택)
 * - 페이징: page + pstartno + onepagelist
 *
 * <p>EmpSearchDto와 동일한 페이징 컨벤션을 따름.
 */
@Data
public class EvalReportSearchDto {

    // ─── 검색 조건 ───
    private Integer periodId;
    private Integer deptId;
    private String  keyword;

    // ─── 회사 격리 (컨트롤러에서 세팅) ───
    private Integer comId;

    // ─── 페이징 ───
    private Integer page;         // 사용자가 요청한 페이지 (1-based)
    private Integer pstartno;     // OFFSET 값 (컨트롤러에서 PagingUtil로 세팅)
    private Integer onepagelist;  // LIMIT 값 (컨트롤러에서 PagingUtil로 세팅)
}