package com.sb.erp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResvImpactDto {
    private Long revId;
    private Long empId;
    private String empName;
    private Long resId;
    private String resName;
    private String status;
    private String startDt;
    private String endDt;
}
