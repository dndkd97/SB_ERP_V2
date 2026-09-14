package com.sb.erp.dto;

import lombok.Data;

@Data
public class ApprFormDto {
	private int forId;
	private int comId;
	private String forCode;
	private String forTitle;
	private String forContent;
	private Boolean forStatus;
	private boolean isDeleted;
	private int forVersion;
	private String createdAt;
	private String updatedAt;
	private String forSchema;
	
	private String comName;
}