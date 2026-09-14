package com.sb.erp.service;

import java.util.List;

import com.sb.erp.dto.ComSearchDto;
import com.sb.erp.dto.CompanyDto;
import com.sb.erp.dto.StatsComDto;

public interface CompanyService {
	public List<CompanyDto> list(ComSearchDto search);
	
	public int add(CompanyDto dto);
	public CompanyDto isDuplicateBizNo(String bizNo);
	
	public CompanyDto selectOneById(int comId);

	public int update(CompanyDto dto);

	public int delete(int comId);

	public List<CompanyDto> getSuggest(String keyword);

	public int listTotal(ComSearchDto search);

	public StatsComDto selectStats();

	public CompanyDto selectOneByEmpId(int empId);
}
