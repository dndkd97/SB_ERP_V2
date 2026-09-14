package com.sb.erp.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sb.erp.dto.ComSearchDto;
import com.sb.erp.dto.CompanyDto;
import com.sb.erp.dto.StatsComDto;

@Mapper
public interface CompanyMapper {
	public List<CompanyDto> selectAll(ComSearchDto search);

	public int insert(CompanyDto dto);
	public CompanyDto selectByBizNo(String bizNo);

	public CompanyDto selectOneById(int comId);

	public int update(CompanyDto dto);

	public int delete(int comId);

	public List<CompanyDto> selectSuggest(@Param("keyword") String keyword);

	public int listTotal(ComSearchDto search);

	public StatsComDto selectStats();

	public CompanyDto selectOneByEmpId(int empId);

}
