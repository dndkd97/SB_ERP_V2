package com.sb.erp.dao;


import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.sb.erp.dto.ApprFormDto;
import com.sb.erp.dto.ApprFormSearchDto;
import com.sb.erp.dto.CompanySearchDto;

@Mapper
public interface ApprFormMapper {
	
	// 공통 
	public List<CompanySearchDto> searchCompany(String keyword);
	public String getCompanyName(int comId);
	
	// 페이징 기능
	public int listFormCnt(ApprFormSearchDto dto);
	
	// 양식 파트
	public ApprFormDto selectFormAll(ApprFormDto dto);
	public int insertForm(ApprFormDto dto);
	public int updateForm(ApprFormDto dto);
	public int deleteForm(ApprFormDto dto);
	public int updateFormNewVersion(ApprFormDto dto);
	public List<ApprFormDto> selectFormList(ApprFormSearchDto dto);
	public String findByCode(ApprFormDto dto);
	public List<ApprFormDto> selectFormVersions(int forId);
	
}
