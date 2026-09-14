package com.sb.erp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PendingDeptDto {
    private Integer deptId;
    private String deptName;
    private String deptCode;
    private Integer empCount;
    private String updatedAt; // dept_status가 PENDING_DELETE로 바뀐 시점 (department.updated_at 재사용)
}
