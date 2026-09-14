package com.sb.erp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmpTransferDto {
    private Long empId;
    private String empNo;
    private String empName;
    private String posName;
    private Integer posOrder;
    private String empStatus;
}
