package com.sb.erp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ResDto {
    private Integer resId;           
    private Integer comId;           
    private String resCode;     
    private String resName;      
    private String resType;      
    private Integer quantity;
    //추가된 컬럼
    private String location;
    private Integer capacity;
    private String resStatus;
    
    private Integer managerEmpId;
    private String managerEmpName;
    private String managerEmpNo;
    private String managerPosName;
    
    private String remark;       
    private String createdAt; 
    private String updatedAt;
    
    private Integer resvCount;
    private Integer totQuantity;
    private Integer availQuantity;
}