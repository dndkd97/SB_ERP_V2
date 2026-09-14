package com.sb.erp.dto;

import lombok.Data;

@Data
public class PosDto {
	
	// ─── 직급 정보 ───────────────
	private int posId; // 시스템 PK (시퀀스 자동 채번)
	private String posCode; // 직급 코드
	private String posName; // 직급명
	private int posOrder; // depth 순서
	
	// ─── FK ────────────────────
	private int comId; // 회사 ID
	
}


/*

CREATE TABLE emp_position (
pos_id    NUMBER(10)   NOT NULL,
pos_code  VARCHAR2(20) NOT NULL,
pos_name  VARCHAR2(20) NOT NULL,
pos_order NUMBER(10)   NOT NULL,
com_id    NUMBER(10)   NOT NULL,
CONSTRAINT pk_pos PRIMARY KEY (pos_id),
CONSTRAINT uq_pos_com_code UNIQUE (com_id, pos_code),
CONSTRAINT fk_pos_com FOREIGN KEY (com_id) REFERENCES company (com_id)
);

*/