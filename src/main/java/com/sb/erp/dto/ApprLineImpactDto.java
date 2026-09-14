package com.sb.erp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApprLineImpactDto {
    private Long linId;
    private Long docId;
    private String docTitle;
    private Long empId;
    private String empName;
    private Integer linOrder;
    private String linStatus;
}
