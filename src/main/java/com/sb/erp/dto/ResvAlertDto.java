package com.sb.erp.dto;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ResvAlertDto {
    private Long revId;        // 예약 PK
    private Long resId;        // 자원 PK
 
    private String resCode;    // 자원 코드 (예: 차량번호로 활용)
    private String resName;    // 자원명 (예: 아반떼, 3층 대회의실)
    private String resType;    // ROOM / VEHICLE / EQUIPMENT
 
    private Long empId;        // 신청자 사원 PK
    private String empName;    // 신청자 이름
    private String empMobile;  // 신청자 휴대폰 번호 (CoolSMS 발신 대상)
 
    private Timestamp startDt;
    private Timestamp endDt;
    private Timestamp returnDt;
}
