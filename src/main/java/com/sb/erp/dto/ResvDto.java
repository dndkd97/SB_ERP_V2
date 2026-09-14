package com.sb.erp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ResvDto {
    private Integer revId;
    private Integer resId;
    private Integer comId;
    private Integer empId;
    
    private Integer quantity;
    private String status;
    
    //추가 컬럼
    private String startDt;
    private String endDt;
    private String returnDt;
    private Integer approvedEmpId;
    private String approvedAt;
    private String rejectReason;
    
    private String remark;
    private String createdAt;
    private String updatedAt;
    
    //
    private String resName;
    private String resCode;
    private String resType;
    private String location;
    
    private String empName;
    private String deptName;
    private String approvedEmpName;
}
