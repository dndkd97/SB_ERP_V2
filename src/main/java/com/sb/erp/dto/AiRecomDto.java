package com.sb.erp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AiRecomDto {
    private Long targetDeptId;
    private String targetDeptName; // 서비스에서 candidates 목록과 매칭해 채워줌 (화면 표시용)
    private String reason;
}
