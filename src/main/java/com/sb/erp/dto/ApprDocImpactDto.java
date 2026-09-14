package com.sb.erp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApprDocImpactDto {
    private Long docId;
    private String docTitle;
    private Long empId;
    private String empName;
    private String docStatus;
}
